package com.trss.bi.web.rest.util;

import java.util.Map;
import java.util.stream.Collectors;

public class RequestParamUtil {

    private static final String PARAM_CACHEBUSTER = "cacheBuster";
    private static final String PARAM_PAGEABLE_SIZE = "size";
    private static final String PARAM_PAGEABLE_PAGE = "page";
    private static final String PARAM_PAGEABLE_SORT = "sort";

    /**
     * Remove the pageable (size, page, sort) and cacheBuster params from the map.
     *
     * @param params
     * @return  a new map with the pageable and cacheBuster params removed
     */
    public static Map<String, String> removePageableParams(Map<String,String> params) {
        return params.entrySet()
            .stream()
            .filter(paramEntry -> !(paramEntry.getKey().equals(PARAM_CACHEBUSTER) ||
                                    paramEntry.getKey().equals(PARAM_PAGEABLE_SIZE) ||
                                    paramEntry.getKey().equals(PARAM_PAGEABLE_PAGE) ||
                                    paramEntry.getKey().equals(PARAM_PAGEABLE_SORT))
            )
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
