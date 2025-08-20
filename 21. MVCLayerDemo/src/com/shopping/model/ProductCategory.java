package com.shopping.model;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 상품 카테고리를 한글로 직접 정의하는 열거형 클래스.
 */
public enum ProductCategory {
    전자제품,
    의류,
    식품,
    도서,
    기타;

    /**
     * 문자열로부터 해당하는 Enum 상수를 찾아 반환합니다.
     * @param name 찾고자 하는 카테고리 문자열
     * @return 해당하는 ProductCategory Enum. 없으면 IllegalArgumentException 발생.
     */
    public static ProductCategory fromString(String name) {
        try {
            // valueOf 메소드는 주어진 이름과 일치하는 enum 상수를 반환합니다.
            return ProductCategory.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + name);
        }
    }

    /**
     * 모든 카테고리 이름을 문자열로 반환합니다. (예: "전자제품, 의류, ...")
     * @return 콤마로 구분된 카테고리 목록 문자열
     */
    public static String getCategoryNames() {
        return Stream.of(ProductCategory.values())
                     .map(ProductCategory::name)
                     .collect(Collectors.joining(", "));
    }
}