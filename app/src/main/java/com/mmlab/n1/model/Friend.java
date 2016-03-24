package com.mmlab.n1.model;

import io.realm.RealmObject;

/**
 * Created by waynewei on 2015/10/25.
 */
public class Friend extends RealmObject {

	private String name;
	private String id;
	private String photoUrl;

	public Friend() {

	}

	public void setName(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setPhotoUrl(String photoUrl) {
		 this.photoUrl = photoUrl;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}
}
