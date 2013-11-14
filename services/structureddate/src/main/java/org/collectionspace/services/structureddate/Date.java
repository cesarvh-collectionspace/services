package org.collectionspace.services.structureddate;

import org.apache.commons.lang.builder.EqualsBuilder;

public class Date {
	public static final Integer DEFAULT_YEAR = null;
	public static final Integer DEFAULT_MONTH = null;
	public static final Integer DEFAULT_DAY = null;
	public static final Era DEFAULT_ERA = Era.CE;
	public static final Certainty DEFAULT_CERTAINTY = null;
	public static final QualifierType DEFAULT_QUALIFIER = null;
	public static final Integer DEFAULT_QUALIFIER_VALUE = null;
	public static final QualifierUnit DEFAULT_QUALIFIER_UNIT = null;

	private Integer year;
	private Integer month;
	private Integer day;
	private Era era;
	private Certainty certainty;
	private QualifierType qualifierType;
	private Integer qualifierValue;
	private QualifierUnit qualifierUnit;
	private String scalarValue;

	public Date() {
		this(DEFAULT_YEAR, DEFAULT_MONTH, DEFAULT_DAY, DEFAULT_ERA, DEFAULT_CERTAINTY, DEFAULT_QUALIFIER, DEFAULT_QUALIFIER_VALUE, DEFAULT_QUALIFIER_UNIT);
	}
	
	public Date(Integer year, Integer month, Integer day) {
		this(year, month, day, DEFAULT_ERA, DEFAULT_CERTAINTY, DEFAULT_QUALIFIER, DEFAULT_QUALIFIER_VALUE, DEFAULT_QUALIFIER_UNIT);
	}

	public Date(Integer year, Integer month, Integer day, Era era, Certainty certainty, QualifierType qualifierType, Integer qualifierValue, QualifierUnit qualifierUnit) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.era = era;
		this.certainty = certainty;
		this.qualifierType = qualifierType;
		this.qualifierValue = qualifierValue;
		this.qualifierUnit = qualifierUnit;
	}
	
	public Date withEra(Era era) {
		this.setEra(era);
		
		return this;
	}
	
	public Date withQualifier(QualifierType qualifierType, Integer qualifierValue, QualifierUnit qualifierUnit) {
		this.setQualifier(qualifierType, qualifierValue, qualifierUnit);

		return this;
	}
		
	public String toString() {
		return
			"\t\tyear:           " + getYear() + "\n" +
			"\t\tmonth:          " + getMonth() + "\n" + 
			"\t\tday:            " + getDay() + "\n" +
			"\t\tera:            " + getEra() + "\n" +
			"\t\tcertainty:      " + getCertainty() + "\n" +
			"\t\tqualifierType:  " + getQualifierType() + "\n" +
			"\t\tqualifierValue: " + getQualifierValue() + "\n" +
			"\t\tqualifierUnit:  " + getQualifierUnit() + "\n" +
			"\t\tscalarValue:    " + getScalarValue() + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { 
			return false;
		}
		
		if (obj == this) {
			return true;
		}
		
		if (obj.getClass() != getClass()) {
			return false;
		}
		
		Date that = (Date) obj;

		return
			new EqualsBuilder()
				.append(this.getYear(), that.getYear())
				.append(this.getMonth(), that.getMonth())
				.append(this.getDay(), that.getDay())
				.append(this.getEra(), that.getEra())
				.append(this.getCertainty(), that.getCertainty())
				.append(this.getQualifierType(), that.getQualifierType())
				.append(this.getQualifierValue(), that.getQualifierValue())
				.append(this.getQualifierUnit(), that.getQualifierUnit())
				.append(this.getScalarValue(), that.getScalarValue())
				.isEquals();
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public Era getEra() {
		return era;
	}

	public void setEra(Era era) {
		this.era = era;
	}

	public Certainty getCertainty() {
		return certainty;
	}

	public void setCertainty(Certainty certainty) {
		this.certainty = certainty;
	}

	public QualifierType getQualifierType() {
		return qualifierType;
	}

	public void setQualifierType(QualifierType qualifierType) {
		this.qualifierType = qualifierType;
	}

	public Integer getQualifierValue() {
		return qualifierValue;
	}

	public void setQualifierValue(Integer qualifierValue) {
		this.qualifierValue = qualifierValue;
	}

	public QualifierUnit getQualifierUnit() {
		return qualifierUnit;
	}

	public void setQualifierUnit(QualifierUnit qualifierUnit) {
		this.qualifierUnit = qualifierUnit;
	}

	public void setQualifier(QualifierType qualifierType, Integer qualifierValue, QualifierUnit qualifierUnit) {
		this.qualifierType = qualifierType;
		this.qualifierValue = qualifierValue;
		this.qualifierUnit = qualifierUnit;
	}
	
	public String getScalarValue() {
		return scalarValue;
	}

	public void setScalarValue(String scalarValue) {
		this.scalarValue = scalarValue;
	}
}

