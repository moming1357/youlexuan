package com.ujiuye.pojogroup;

import java.io.Serializable;
import java.util.List;

import com.ujiuye.pojo.TbSpecification;
import com.ujiuye.pojo.TbSpecificationOption;

public class Specification implements Serializable{

	private TbSpecification specification;
	private List<TbSpecificationOption> specificationOptionList;

	public TbSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(TbSpecification specification) {
		this.specification = specification;
	}

	public List<TbSpecificationOption> getSpecificationOptionList() {
		return specificationOptionList;
	}

	public void setSpecificationOptionList(
			List<TbSpecificationOption> specificationOptionList) {
		this.specificationOptionList = specificationOptionList;
	}

	public Specification() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Specification(TbSpecification specification,
			List<TbSpecificationOption> specificationOptionList) {
		super();
		this.specification = specification;
		this.specificationOptionList = specificationOptionList;
	}

}
