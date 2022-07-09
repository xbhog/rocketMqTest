package com.xbhog.Mapper;

import com.xbhog.pojo.Jd;
import com.xbhog.pojo.TaoBao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/9
 */
@Mapper
public interface ImageOcrClearAndBitchInsertMapper {
    /**
     * 淘宝数据清洗
     * @param currentDate
     * @return
     */
    List<TaoBao> queryTbData(@Param("currentDate") String currentDate);

    /**
     * 京东数据清洗
     * @param currentDate
     * @return
     */
    List<Jd> queryJdData(@Param("currentDate") String currentDate);
}
