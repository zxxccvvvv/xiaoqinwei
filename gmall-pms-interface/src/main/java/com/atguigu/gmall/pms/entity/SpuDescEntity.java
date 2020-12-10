package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * spu信息介绍
 * 
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:49
 */
@Data
@TableName("pms_spu_desc")
public class SpuDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 */
	//由于在配置文件中设置的是主键自增策略，因此需要在这了把主键策略改成手动设置
	@TableId(type = IdType.INPUT)
	private Long spuId;
	/**
	 * 商品介绍
	 */
	private String decript;

}
