package com.ppdai.stargate.utils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.vo.PageVO;

public class ConvertUtil {

    public static <S, T> T convert(S s, Class<T> tClass) {
        try {
            T t = tClass.newInstance();
            BeanUtils.copyProperties(s, t);
            return t;
        } catch (Exception e) {
            throw BaseException.newException(MessageType.ERROR, "convert error");
        }
    }

    public static <S, T> T convert(S s, T t) {
        try {
            BeanUtils.copyProperties(s, t);
            return t;
        } catch (Exception e) {
            throw BaseException.newException(MessageType.ERROR, "convert error");
        }
    }

    public static <S, T> List<T> convert(Iterable<S> iterable, Class<T> tClass) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(s -> ConvertUtil.convert(s, tClass)).collect(Collectors.toList());
    }

    public static <S, T> List<T> convert(Iterable<S> iterable, Function<? super S, ? extends T> mapper) {
        return StreamSupport.stream(iterable.spliterator(), false).map(mapper).collect(Collectors.toList());
    }

    public static <S, T> PageVO<T> convertPage(Page<S> page, Function<? super S, ? extends T> mapper) {
        List<S> contents = page.getContent();
        List<T> tContents = convert(contents, mapper);
        PageVO<T> pageDto = new PageVO<>();
        pageDto.setContent(tContents);
        pageDto.setFirst(page.isFirst());
        pageDto.setLast(page.isLast());
        pageDto.setNumber(page.getNumber());
        pageDto.setNumberOfElements(page.getNumberOfElements());
        pageDto.setTotalPages(page.getTotalPages());
        pageDto.setTotalElements(page.getTotalElements());
        pageDto.setSize(page.getSize());
        return pageDto;
    }

    public static <S, T> PageVO<T> convertPage(Page<S> page, Class<T> tClass) {
        return convertPage(page, s -> convert(s, tClass));
    }
}
