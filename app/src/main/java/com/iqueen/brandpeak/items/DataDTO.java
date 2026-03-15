package com.sgdigitalposter.app.items;

import java.io.Serializable;

public class DataDTO implements Serializable {
	private String merchantId;
	private String merchantTransactionId;
	private String transactionId;
	private int amount;
	private String state;
	private String responseCode;
	private PaymentInstrumentDTO paymentInstrument;

	public void setMerchantId(String merchantId){
		this.merchantId = merchantId;
	}

	public String getMerchantId(){
		return merchantId;
	}

	public void setMerchantTransactionId(String merchantTransactionId){
		this.merchantTransactionId = merchantTransactionId;
	}

	public String getMerchantTransactionId(){
		return merchantTransactionId;
	}

	public void setTransactionId(String transactionId){
		this.transactionId = transactionId;
	}

	public String getTransactionId(){
		return transactionId;
	}

	public void setAmount(int amount){
		this.amount = amount;
	}

	public int getAmount(){
		return amount;
	}

	public void setState(String state){
		this.state = state;
	}

	public String getState(){
		return state;
	}

	public void setResponseCode(String responseCode){
		this.responseCode = responseCode;
	}

	public String getResponseCode(){
		return responseCode;
	}

	public void setPaymentInstrument(PaymentInstrumentDTO paymentInstrument){
		this.paymentInstrument = paymentInstrument;
	}

	public PaymentInstrumentDTO getPaymentInstrument(){
		return paymentInstrument;
	}

	@Override
 	public String toString(){
		return 
			"DataDTO{" + 
			"merchantId = '" + merchantId + '\'' + 
			",merchantTransactionId = '" + merchantTransactionId + '\'' + 
			",transactionId = '" + transactionId + '\'' + 
			",amount = '" + amount + '\'' + 
			",state = '" + state + '\'' + 
			",responseCode = '" + responseCode + '\'' + 
			",paymentInstrument = '" + paymentInstrument + '\'' + 
			"}";
		}
}