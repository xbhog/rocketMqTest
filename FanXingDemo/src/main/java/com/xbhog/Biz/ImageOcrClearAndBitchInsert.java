package com.xbhog.Biz;
import com.xbhog.pojo.Jd;
import com.xbhog.pojo.TaoBao;

import java.util.List;

/**
 * @author xbhog
 * @describe:OCR清洗和批量插入
 * @date 2022/7/9
 */

public interface ImageOcrClearAndBitchInsert {
    /**
     * 淘宝数据查询
     */
    List<TaoBao> queryTbData(String currentDate);
    /**
     * 京东数据查询
     */
    List<Jd> queryJdData(String currentDate);

}
