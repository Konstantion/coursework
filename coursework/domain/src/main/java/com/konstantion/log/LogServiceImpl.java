package com.konstantion.log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.EquipmentPort;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.FileIOException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.utils.ExceptionUtils;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.file.PdfUtils;
import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
import com.konstantion.guest.Guest;
import com.konstantion.guest.GuestPort;
import com.konstantion.log.model.CreateBillRequest;
import com.konstantion.user.User;
import com.konstantion.user.UserPort;
import com.konstantion.utils.DoubleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.CANCEL_BILL;
import static com.konstantion.user.Permission.CHANGE_BILL_STATE;
import static com.konstantion.user.Permission.CLOSE_BILL;
import static com.konstantion.user.Permission.CREATE_BILL_FROM_ORDER;
import static com.konstantion.user.Permission.SUPER_USER;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;

@Component
public record LogServiceImpl(
        EquipmentPort equipmentPort,
        ExpeditionPort expeditionPort,
        GuestPort guestPort,
        UserPort userPort,
        GearPort gearPort,
        LogPort logPort
) implements LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    @Override
    public List<Log> getAll(boolean onlyActive) {
        List<Log> logs = logPort.findAll();

        if (onlyActive) {
            return logs.stream().filter(Log::isActive).toList();
        }
        logger.info("Logs successfully returned");
        return logs;
    }

    @Override
    public Log getById(UUID id) {
        Log log = getByIdOrThrow(id);
        logger.info("Log with id {} successfully returned", id);
        return log;
    }

    @Override
    public Log create(CreateBillRequest createBillRequest, User user) {
        if (user.hasNoPermission(CREATE_BILL_FROM_ORDER)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Equipment equipment = equipmentPort.findById(createBillRequest.orderId())
                .orElseThrow(nonExistingIdSupplier(Equipment.class, createBillRequest.orderId()));
        ExceptionUtils.isActiveOrThrow(equipment);

        if (equipment.hasBill()) {
            throw new BadRequestException(format("Equipment with id %s already has log", equipment.getId()));
        }

        if (equipment.getGearsId().isEmpty()) {
            throw new BadRequestException(format("Cannot create log for equipment with id %s because it doesn't contain any gear", equipment.getId()));
        }

        Guest guest = guestOrNull(createBillRequest.guestId());

        Double price = calculateOrderPrice(equipment);
        Double priceWithDiscount = calculatePriceWithDiscount(price, guest);

        Log log = Log.builder()
                .guideId(user.getId())
                .equipmentId(equipment.getId())
                .guestId(createBillRequest.guestId())
                .price(price)
                .priceWithDiscount(priceWithDiscount)
                .active(true)
                .createdAt(now())
                .build();
        logPort.save(log);

        equipment.setLogId(log.getId());
        equipmentPort.save(equipment);

        logger.info("Log successfully created and returned");
        return log;
    }

    @Override
    public Log cancel(UUID billId, User user) {
        if (user.hasNoPermission(CANCEL_BILL)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Log log = getByIdOrThrow(billId);

        Equipment equipment = equipmentPort.findById(log.getEquipmentId())
                .orElseThrow(nonExistingIdSupplier(Equipment.class, log.getEquipmentId()));

        equipment.removeBill();

        activateOrder(log);

        equipmentPort.save(equipment);
        logPort.delete(log);


        logger.info("Log with id {} successfully canceled and returned", billId);
        return log;
    }

    @Override
    public Log close(UUID billId, User user) {
        if (user.hasNoPermission(CLOSE_BILL)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Log log = getByIdOrThrow(billId);
        if (!log.isActive()) {
            logger.warn("Log with id {} is already closed and returned", billId);
            return log;
        }

        prepareToClose(log);

        logPort.save(log);
        logger.info("Log with id {} successfully canceled and returned", billId);
        return log;
    }

    @Override
    public Log activate(UUID billId, User user) {
        if (user.hasNoPermission(CHANGE_BILL_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Log log = getByIdOrThrow(billId);

        if (log.isActive()) {
            logger.warn("Log with id {} is already active and returned", billId);
            return log;
        }

        prepareToActivate(log);

        activateOrder(log);

        logPort.save(log);

        logger.info("Log with id {} successfully activated and returned", billId);
        return log;
    }

    private void activateOrder(Log log) {
        Equipment equipment = equipmentPort.findById(log.getEquipmentId())
                .orElseThrow(nonExistingIdSupplier(Equipment.class, log.getEquipmentId()));

        if (!equipment.isActive()) {
            equipment.setActive(true);
            equipment.setClosedAt(null);
            equipmentPort.save(equipment);
        }
    }

    @Override
    public byte[] getPdfBytesById(UUID id, User user) {
        Log log = getByIdOrThrow(id);
        Equipment equipment = equipmentPort.findById(log.getEquipmentId()).orElseThrow(nonExistingIdSupplier(Equipment.class, log.getEquipmentId()));
        Map<Gear, Long> products = productsMap(equipment.getGearsId());
        Guest guest = guestPort.findById(log.getGuestId()).orElse(null);
        Expedition table = expeditionPort.findById(equipment.getExpeditionId()).orElse(null);
        User waiter = userPort.findById(log.getGuideId()).orElse(null);

        try {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();
            PdfUtils.fillBillDocumentPdf(
                    log,
                    equipment,
                    products,
                    table,
                    waiter,
                    guest,
                    document
            );

            document.close();
            return fos.toByteArray();
        } catch (DocumentException e) {
            throw new FileIOException(e.getMessage());
        }
    }

    private Log getByIdOrThrow(UUID id) {
        return logPort.findById(id)
                .orElseThrow(nonExistingIdSupplier(Log.class, id));
    }

    private Double calculateOrderPrice(Equipment equipment) {
        Double orderPrice = equipment.getGearsId().stream()
                .map(gearPort::findById)
                .map(Optional::orElseThrow)
                .map(Gear::getPrice)
                .reduce(0.0, Double::sum);

        return DoubleUtils.round(orderPrice, 2);
    }

    private Map<Gear, Long> productsMap(List<UUID> productIds) {
        Map<UUID, Gear> productsMap = new HashSet<>(productIds).stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            Gear gear = gearPort.findById(id).orElseThrow();
                            gear.setImageBytes(null);
                            return gear;
                        }
                ));

        return productIds.stream()
                .map(productsMap::get)
                .collect(Collectors.toMap(
                        product -> product,
                        product -> 1L,
                        Long::sum
                ));
    }

    /**
     * Method null save for parameter <b style='color: red'>guest</b>
     */
    private Double calculatePriceWithDiscount(Double orderPrice, @Nullable Guest guest) {
        double discountPercent = 0.0;
        if (nonNull(guest)) {
            discountPercent = max(0.0, min(guest.getDiscountPercent(), 100.0));
        }
        double discountedPrice = orderPrice - DoubleUtils.percent(orderPrice, discountPercent);

        return DoubleUtils.round(discountedPrice, 2);
    }

    private void prepareToClose(Log log) {
        log.setClosedAt(now());
        log.setActive(false);
    }

    private void prepareToActivate(Log log) {
        log.setClosedAt(null);
        log.setActive(true);
    }


    private Guest guestOrNull(UUID guestId) {
        boolean guestPresent = guestId != null;
        Guest guest = null;
        if (guestPresent) {
            guest = guestPort.findById(guestId)
                    .orElseThrow(nonExistingIdSupplier(Guest.class, guestId));
            ExceptionUtils.isActiveOrThrow(guest);
        }
        return guest;
    }
}
