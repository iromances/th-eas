/**
 * WSWSRtnInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.thchengtay.eas.model.eas;

public class WSWSRtnInfo extends WSBean implements java.io.Serializable {
    private String description;

    private String easNumber;

    private String exception;

    private String message;

    private String number;

    private int period;

    private String voucherType;

    private int year;

    public WSWSRtnInfo() {
    }

    public WSWSRtnInfo(
           String description,
           String easNumber,
           String exception,
           String message,
           String number,
           int period,
           String voucherType,
           int year) {
        this.description = description;
        this.easNumber = easNumber;
        this.exception = exception;
        this.message = message;
        this.number = number;
        this.period = period;
        this.voucherType = voucherType;
        this.year = year;
    }


    /**
     * Gets the description value for this WSWSRtnInfo.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this WSWSRtnInfo.
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Gets the easNumber value for this WSWSRtnInfo.
     * 
     * @return easNumber
     */
    public String getEasNumber() {
        return easNumber;
    }


    /**
     * Sets the easNumber value for this WSWSRtnInfo.
     * 
     * @param easNumber
     */
    public void setEasNumber(String easNumber) {
        this.easNumber = easNumber;
    }


    /**
     * Gets the exception value for this WSWSRtnInfo.
     * 
     * @return exception
     */
    public String getException() {
        return exception;
    }


    /**
     * Sets the exception value for this WSWSRtnInfo.
     * 
     * @param exception
     */
    public void setException(String exception) {
        this.exception = exception;
    }


    /**
     * Gets the message value for this WSWSRtnInfo.
     * 
     * @return message
     */
    public String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this WSWSRtnInfo.
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }


    /**
     * Gets the number value for this WSWSRtnInfo.
     * 
     * @return number
     */
    public String getNumber() {
        return number;
    }


    /**
     * Sets the number value for this WSWSRtnInfo.
     * 
     * @param number
     */
    public void setNumber(String number) {
        this.number = number;
    }


    /**
     * Gets the period value for this WSWSRtnInfo.
     * 
     * @return period
     */
    public int getPeriod() {
        return period;
    }


    /**
     * Sets the period value for this WSWSRtnInfo.
     * 
     * @param period
     */
    public void setPeriod(int period) {
        this.period = period;
    }


    /**
     * Gets the voucherType value for this WSWSRtnInfo.
     * 
     * @return voucherType
     */
    public String getVoucherType() {
        return voucherType;
    }


    /**
     * Sets the voucherType value for this WSWSRtnInfo.
     * 
     * @param voucherType
     */
    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }


    /**
     * Gets the year value for this WSWSRtnInfo.
     * 
     * @return year
     */
    public int getYear() {
        return year;
    }


    /**
     * Sets the year value for this WSWSRtnInfo.
     * 
     * @param year
     */
    public void setYear(int year) {
        this.year = year;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof WSWSRtnInfo)) return false;
        WSWSRtnInfo other = (WSWSRtnInfo) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.easNumber==null && other.getEasNumber()==null) || 
             (this.easNumber!=null &&
              this.easNumber.equals(other.getEasNumber()))) &&
            ((this.exception==null && other.getException()==null) || 
             (this.exception!=null &&
              this.exception.equals(other.getException()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.number==null && other.getNumber()==null) || 
             (this.number!=null &&
              this.number.equals(other.getNumber()))) &&
            this.period == other.getPeriod() &&
            ((this.voucherType==null && other.getVoucherType()==null) || 
             (this.voucherType!=null &&
              this.voucherType.equals(other.getVoucherType()))) &&
            this.year == other.getYear();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getEasNumber() != null) {
            _hashCode += getEasNumber().hashCode();
        }
        if (getException() != null) {
            _hashCode += getException().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getNumber() != null) {
            _hashCode += getNumber().hashCode();
        }
        _hashCode += getPeriod();
        if (getVoucherType() != null) {
            _hashCode += getVoucherType().hashCode();
        }
        _hashCode += getYear();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WSWSRtnInfo.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://app.gl.fi.eas.kingdee.com", "WSWSRtnInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("easNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "easNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exception");
        elemField.setXmlName(new javax.xml.namespace.QName("", "exception"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("", "message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("number");
        elemField.setXmlName(new javax.xml.namespace.QName("", "number"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("period");
        elemField.setXmlName(new javax.xml.namespace.QName("", "period"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("voucherType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "voucherType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("year");
        elemField.setXmlName(new javax.xml.namespace.QName("", "year"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
