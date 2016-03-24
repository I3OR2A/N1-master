package com.mmlab.n1.model;

import io.realm.RealmObject;

/**
 * Created by waynewei on 2015/10/25.
 */
public class MyFavorite extends RealmObject {

	private String title;
	private String id;
	private String pic;

	public MyFavorite(){

	}

	public void setId(String id){
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setPic(String pic){
		this.pic = pic;
	}

	public String getPic() {
		return pic;
	}
}
