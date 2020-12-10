package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SpuDescService;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("spuDescService")
public class SpuDescServiceImpl extends ServiceImpl<SpuDescMapper, SpuDescEntity> implements SpuDescService {

    @Autowired
    private SpuDescMapper spuDescMapper;
    //@Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveSpuDesc(SpuVo spuVo, Long spuId) {
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(spuId);
        // 注意：spu_info_desc表的主键是spu_id,需要在实体类中配置该主键不是自增主键

        // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
        spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
        spuDescMapper.insert(spuDescEntity);
    }



    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuDescEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuDescEntity>()
        );

        return new PageResultVo(page);
    }

}