package com.example.dto;

public class RezultatiGlasanjaDTO {


	private String raw_id; // raw_id akta ili amandmana
	private int za;
	private int protiv;
	private int uzdrzano;
	private double procenat;
	private String osnovDonosenja;
	
	public RezultatiGlasanjaDTO() { }
	

	public String getRaw_id() {
		return raw_id;
	}

	public void setRaw_id(String raw_id) {
		this.raw_id = raw_id;
	}

	public int getZa() {
		return za;
	}

	public void setZa(int za) {
		this.za = za;
	}

	public int getProtiv() {
		return protiv;
	}

	public void setProtiv(int protiv) {
		this.protiv = protiv;
	}

	public int getUzdrzano() {
		return uzdrzano;
	}

	public void setUzdrzano(int uzdrzano) {
		this.uzdrzano = uzdrzano;
	}

	public double getProcenat() {
		return procenat;
	}

	public void setProcenat(double procenat) {
		this.procenat = procenat;
	}

	public String getOsnovDonosenja() {
		return osnovDonosenja;
	}

	public void setOsnovDonosenja(String osnovDonosenja) {
		this.osnovDonosenja = osnovDonosenja;
	}
}
