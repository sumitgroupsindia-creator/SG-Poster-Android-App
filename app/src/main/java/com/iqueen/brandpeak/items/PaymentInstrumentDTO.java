package com.sgdigitalposter.app.items;

import java.io.Serializable;

public class PaymentInstrumentDTO implements Serializable {
	private String type;
	private String cardType;
	private String pgTransactionId;
	private Object bankTransactionId;
	private Object pgAuthorizationCode;
	private Object arn;
	private String bankId;
	private String brn;

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setCardType(String cardType){
		this.cardType = cardType;
	}

	public String getCardType(){
		return cardType;
	}

	public void setPgTransactionId(String pgTransactionId){
		this.pgTransactionId = pgTransactionId;
	}

	public String getPgTransactionId(){
		return pgTransactionId;
	}

	public void setBankTransactionId(Object bankTransactionId){
		this.bankTransactionId = bankTransactionId;
	}

	public Object getBankTransactionId(){
		return bankTransactionId;
	}

	public void setPgAuthorizationCode(Object pgAuthorizationCode){
		this.pgAuthorizationCode = pgAuthorizationCode;
	}

	public Object getPgAuthorizationCode(){
		return pgAuthorizationCode;
	}

	public void setArn(Object arn){
		this.arn = arn;
	}

	public Object getArn(){
		return arn;
	}

	public void setBankId(String bankId){
		this.bankId = bankId;
	}

	public String getBankId(){
		return bankId;
	}

	public void setBrn(String brn){
		this.brn = brn;
	}

	public String getBrn(){
		return brn;
	}

	@Override
 	public String toString(){
		return 
			"PaymentInstrumentDTO{" + 
			"type = '" + type + '\'' + 
			",cardType = '" + cardType + '\'' + 
			",pgTransactionId = '" + pgTransactionId + '\'' + 
			",bankTransactionId = '" + bankTransactionId + '\'' + 
			",pgAuthorizationCode = '" + pgAuthorizationCode + '\'' + 
			",arn = '" + arn + '\'' + 
			",bankId = '" + bankId + '\'' + 
			",brn = '" + brn + '\'' + 
			"}";
		}
}