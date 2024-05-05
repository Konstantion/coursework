package com.konstantion.utils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleUtils {
    private RoleUtils() {
    }

    public static Set<String> generateSetOfCombinationWithPrefixWordAndCollectionEnum(CharSequence prefixWord,
                                                                                      Collection<? extends Enum> collection,
                                                                                      CharSequence separator) {
        Collection<CharSequence> enumNames = collection.stream().map(Enum::name).collect(Collectors.toSet());
        return generateSetOfCombinationWithPrefixWordAndCollection(prefixWord, enumNames, separator);
    }

    public static Set<String> generateSetOfCombinationWithPrefixWordAndCollection(CharSequence prefixWord,
                                                                                  Collection<? extends CharSequence> collection,
                                                                                  CharSequence separator) {
        return collection.stream()
                .map(element -> String.join(separator, prefixWord, element))
                .collect(Collectors.toSet());
    }
}
