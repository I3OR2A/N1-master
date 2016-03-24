package com.mmlab.n1.model;

import io.realm.RealmObject;

/**
 * Created by waynewei on 2016/3/22.
 */
public class DEHUser extends RealmObject {

	private String id;

	public void setId(String id){
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
