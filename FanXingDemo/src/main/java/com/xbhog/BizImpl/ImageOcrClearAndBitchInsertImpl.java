package com.xbhog.BizImpl;

import com.xbhog.Biz.ImageOcrClearAndBitchInsert;
import com.xbhog.Mapper.ImageOcrClearAndBitchInsertMapper;
import com.xbhog.pojo.Jd;
import com.xbhog.pojo.TaoBao;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/9
 */

public class ImageOcrClearAndBitchInsertImpl implements ImageOcrClearAndBitchInsert {
    @Resource
    private ImageOcrClearAndBitchInsertMapper mapper;

    @Override
    public List<TaoBao> queryTbData(String currentDate) {
        return mapper.queryTbData(currentDate);
    }

    @Override
    public List<Jd> queryJdData(String currentDate) {
        return mapper.queryJdData(currentDate);
    }
}
