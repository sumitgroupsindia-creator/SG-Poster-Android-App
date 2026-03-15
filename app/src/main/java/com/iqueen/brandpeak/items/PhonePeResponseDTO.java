package com.sgdigitalposter.app.items;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PhonePeResponseDTO implements Serializable {

	@SerializedName("success")
	private boolean success;

	@SerializedName("code")
	private String code;

	@SerializedName("message")
	private String message;

	@SerializedName("data")
	private DataDTO data;

	public void setSuccess(boolean success){
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	public void setCode(String code){
		this.code = code;
	}

	public String getCode(){
		return code;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setData(DataDTO data){
		this.data = data;
	}

	public DataDTO getData(){
		return data;
	}

	@Override
 	public String toString(){
		return 
			"PhonePeResponseDTO{" + 
			"success = '" + success + '\'' + 
			",code = '" + code + '\'' + 
			",message = '" + message + '\'' + 
			",data = '" + data + '\'' + 
			"}";
		}
}