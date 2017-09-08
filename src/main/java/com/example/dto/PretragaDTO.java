package com.example.dto;

public class PretragaDTO {

	private String naziv;
	private String status;
	private String datumOd;
	private String datumDo;
	private double procenat;
	private String odbornik;
	
	public PretragaDTO(){ }

	public PretragaDTO(String naziv, String status, String datumOd, String datumDo, double procenat, String odbornik) {
		super();
		this.naziv = naziv;
		this.status = status;
		this.datumOd = datumOd;
		this.datumDo = datumDo;
		this.procenat = procenat;
		this.odbornik = odbornik;
	}

	@Override
	public String toString() {
		return "PretragaDTO [naziv=" + naziv + ", status=" + status + ", datumOd=" + datumOd + ", datumDo=" + datumDo
				+ ", procenat=" + procenat + ", odbornik=" + odbornik + "]";
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDatumOd() {
		return datumOd;
	}

	public void setDatumOd(String datumOd) {
		this.datumOd = datumOd;
	}

	public String getDatumDo() {
		return datumDo;
	}

	public void setDatumDo(String datumDo) {
		this.datumDo = datumDo;
	}

	public double getProcenat() {
		return procenat;
	}

	public void setProcenat(double procenat) {
		this.procenat = procenat;
	}

	public String getOdbornik() {
		return odbornik;
	}

	public void setOdbornik(String odbornik) {
		this.odbornik = odbornik;
	}
}
