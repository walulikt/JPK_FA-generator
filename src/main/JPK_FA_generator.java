package main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import jpk.CurrCodeType;
import jpk.JPK;
import jpk.JPK.Naglowek;
import jpk.ObjectFactory;
import jpk.TNaglowek.KodFormularza;

public class JPK_FA_generator {

	public static void main(String[] args) {
		JAXBContext jaxbContext = null;
		Marshaller jaxbMarshaller = null;
		
		ObjectFactory oF = new ObjectFactory();
		JPK jpkFA = oF.createJPK();
		
		Naglowek naglowek = oF.createJPKNaglowek();
		KodFormularza kodFormularza = oF.createTNaglowekKodFormularza();
		kodFormularza.setKodSystemowy("JPK_FA (1)");
		kodFormularza.setWersjaSchemy("1-0");
		naglowek.setKodFormularza(kodFormularza);
		byte wariantForm = 01;
		naglowek.setWariantFormularza(wariantForm);
		byte celZlozenia = 01;
		naglowek.setCelZlozenia(celZlozenia);
		
		Calendar gCal = GregorianCalendar.getInstance();
		Date dataOd = new Date(3092384 *1000);
		Date dataDo = new Date(8550135*1000);
		try {
			XMLGregorianCalendar gregDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(gCal.getTime()));
			naglowek.setDataWytworzeniaJPK(gregDate);
			gregDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd").format(dataOd));
			naglowek.setDataOd(gregDate);
			gregDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd").format(dataDo));
			naglowek.setDataOd(gregDate);
		} catch (DatatypeConfigurationException e1) {
			e1.printStackTrace();
		}
		naglowek.setKodUrzedu("value");
		naglowek.setDomyslnyKodWaluty(CurrCodeType.PLN);
		
		
		jpkFA.setNaglowek(naglowek);
		
		File file=null;
		try {

			file = new File("C:\\Users\\TOMEK\\eclipse-workspace\\JPK_FA_GEN\\jpk_fa.xml");
			jaxbContext = JAXBContext.newInstance(JPK.class);
			jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(jpkFA, file);
			jaxbMarshaller.marshal(jpkFA, System.out);

		      } catch (JAXBException e) {
			e.printStackTrace();
		      }

//-----------------------sprawdzanie poprawnoœci xml do xsd		
		
		XML_validator validator = new XML_validator();
		
		String xmlPath="./jpk_fa.xml";
		String xsdPath="./Schemat_JPK_FA(1)_v1-0.xsd";
		//System.out.println(xmlString);
		try {
			validator.validate(xmlPath,xsdPath );
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
