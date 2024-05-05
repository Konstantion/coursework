import {HallDto} from "../dto/hall/hall-dto";

export interface HallResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { hall?: HallDto, halls?: HallDto[] };
}
