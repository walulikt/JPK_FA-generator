

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import jpk.CurrCodeType;
import jpk.JPK;
import jpk.JPK.Faktura;
import jpk.JPK.Naglowek;
import jpk.JPK.Podmiot1;
import jpk.MSCountryCodeType;
import jpk.ObjectFactory;
import jpk.TAdresPolski;
import jpk.TIdentyfikatorOsobyNiefizycznej;
import jpk.TKodKraju;
import jpk.TNaglowek.KodFormularza;


public class JPK_FA_Gen_Manger
{
	private ObjectFactory oFactory = new ObjectFactory();
	byte celZlozenia = 01;
	byte wariantFormularza = 01;
	XMLGregorianCalendar gregDate = null;
	Calendar gCal = GregorianCalendar.getInstance();

	public Naglowek createReadyJPKNaglowek(long dataOd, long dataDo)
	{
		JPK.Naglowek naglowek = oFactory.createJPKNaglowek();

		KodFormularza kodFormularza = oFactory.createTNaglowekKodFormularza();
		kodFormularza.setKodSystemowy("JPK_FA(1)");
		kodFormularza.setWersjaSchemy("1-0");
		naglowek.setKodFormularza(kodFormularza);
		naglowek.setCelZlozenia(celZlozenia);
		naglowek.setWariantFormularza(wariantFormularza);

		try
		{
			gregDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(gCal.getTime()));
			naglowek.setDataWytworzeniaJPK(gregDate);
			naglowek.setDataOd(mapToGregDate(dataOd));
			naglowek.setDataOd(mapToGregDate(dataDo));
		} catch (DatatypeConfigurationException e1)
		{
			e1.printStackTrace();
		}
		naglowek.setKodUrzedu("Wstawi� kod urz�du Skarbowego");
		naglowek.setDomyslnyKodWaluty(CurrCodeType.PLN);

		return naglowek;
	}

	public Podmiot1 creaetReadyJPKPodmiot1(TIdentyfikatorOsobyNiefizycznej identyfikator, TAdresPolski adres)
	{
		JPK.Podmiot1 podmiot = oFactory.createJPKPodmiot1();
		podmiot.setIdentyfikatorPodmiotu(identyfikator);
		podmiot.setAdresPodmiotu(adres);
		return podmiot;
	}

	public TIdentyfikatorOsobyNiefizycznej createReadyIdentyfikatorOsNiefiz(String nip, String nazwa, String regon)
	{
		TIdentyfikatorOsobyNiefizycznej identyfikator = oFactory.createTIdentyfikatorOsobyNiefizycznej();
		identyfikator.setNIP("nip");
		identyfikator.setPelnaNazwa("nazwa");
		identyfikator.setREGON("regon");
		return identyfikator;
	}

	public TAdresPolski createReadyAdresPol(String woj, String pow, String gm, String ul, String nrDomu,
			String nrLokalu, String miejscowosc, String kod, String poczta)
	{
		TAdresPolski adres = oFactory.createTAdresPolski();
		adres.setKodKraju(TKodKraju.PL);
		adres.setWojewodztwo(woj);
		adres.setPowiat(pow);
		adres.setGmina(gm);
		adres.setUlica(ul);
		adres.setNrDomu(nrDomu);
		adres.setNrLokalu(nrLokalu);
		adres.setMiejscowosc(miejscowosc);
		adres.setKodPocztowy(kod);
		adres.setPoczta(poczta);
		return adres;
	}

	public List<Faktura> createFakturaEl(Faktura faktura, ReadOnlyRecord pdgRecord, List<Dokumenty> listaDokDoJpkFa,
			String typFaktury)
	{
		List<Faktura> listaFaktur = new ArrayList<>();
		faktura.setTyp(typFaktury);
	/*!*/	boolean vatRozliczaNabywca = false;			//czy ustawi� pole?
	/*!*/	boolean czySwiadczenieTurystyczne = false;
	/*!*/	boolean czySprzedazSztuki = false;
	/*!*/   BigDecimal testBigDec = new BigDecimal(1);
	/*!*/ 	boolean czyMetodaKasowa = false; 			//ustawi� pole. True je�li klient mia� sprzeda� powy�ej 1,2mln Euro i zg�osi�
	/*!*/ 	boolean czySamofakturowanie = false;		//czy ustawi� pole? klient jest nabywc�, ale wystawia faktur� sprzeda�owa 
	/*!*/ 	boolean	czyJestZwolnienieZVat = false;		//czy ustawi� pole?
	/*!*/	boolean czyFakturaWierzyciela = false; 		//ustawic pole, zrobic na sztywno, zlozyc ze dana z bazy?
	/*!*/	boolean czyFakturaPrzedstawiciela = false;
	/*!*/	boolean czyFakturaPodatnikaDwa = false;
	
		for (int i = 0; i < listaDokDoJpkFa.size(); i++)
		{
			if (listaDokDoJpkFa.get(i).getTypd() == typFaktury) //zlikwidowane zb�dne for
		{

			long dataWystawienia = listaDokDoJpkFa.get(i).getDatawyst().getDayNumber();//sprawdzi� czy getDayNumber == miliseconds from
			long dataSprzedazy = listaDokDoJpkFa.get(i).getDatasprz().getDayNumber();
			faktura.setP1(mapToGregDate(dataWystawienia)); //data wystawienia faktury
			faktura.setP2A(listaDokDoJpkFa.get(i).getNrdok()); //nr faktury
			faktura.setP3A(listaDokDoJpkFa.get(i).getNazwIm());//nazwa nabywcy
			faktura.setP3B(listaDokDoJpkFa.get(i).getAdres()); //adres nabywcy
			faktura.setP3C(pdgRecord.getString(FLD_COMPANY_NAME)); //nazwa sprzedawcy
			faktura.setP3D(pdgRecord.getString(FLD_COMPANY_ADDRESS)); //adres sprzedawcy
			/*kod dopisany*/
			faktura.setP4A(MSCountryCodeType.PL); 								//prefiks podatnika VAT UE sprzedawcy- opcjonalnie
			faktura.setP4B(listaDokDoJpkFa.get(i).get  /*"NIP Sprzedawcy"*/); 	//NIP sprzedawcy
			faktura.setP5A(MSCountryCodeType.PL); 								//prefiks podatnika VAT UE nabywcy - opcjonalnie
			faktura.setP5B(listaDokDoJpkFa.get(i).get /*"NIP Nabywcy"*/); 		//NIP nabywcy
			faktura.setP6(mapToGregDate(dataSprzedazy));						//data sprzeda�y/otrzymania zap�aty
			faktura.setP131(/*listaDokDoJpkFa.get(i).get*/new BigDecimal(101)); //Suma warto�ci sprzeda�y netto ze stawk� podstawow� - aktualnie 23% albo 22%
			faktura.setP141(/*listaDokDoJpkFa.get(i).get*/new BigDecimal(32)); 	//Kwota podatku od sumy warto�ci sprzeda�y netto ze stawk� podstawow� - aktualnie 23% albo 22%
			if(vatRozliczaNabywca==true || czySwiadczenieTurystyczne==true ||czySprzedazSztuki==true ) {
				faktura.setP132(testBigDec); 	//Suma warto�ci sprzeda�y netto ze stawk� obni�on� - 8 % albo 7%
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP142(testBigDec); 	//Kwota podatku od sumy warto�ci sprzeda�y netto ze stawk� obni�on� - 8 % albo 7%
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP133(testBigDec); 	//Suma warto�ci sprzeda�y netto ze stawk� obni�on� drug� - 5%
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP143(testBigDec); 	//Kwota podatku od sumy warto�ci sprzeda�y netto ze stawk� obni�on� drug� - 5%
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP134(testBigDec); 	//Suma warto�ci sprzeda�y netto ze stawk� obni�on� trzeci� - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP144(testBigDec); 	//Kwota podatku od sumy warto�ci sprzeda�y netto ze stawk� obni�on� trzeci� - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP135(testBigDec); 	//Suma warto�ci sprzeda�y netto ze stawk� obni�on� czwart� - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP145(testBigDec); 	//Kwota podatku od sumy warto�ci sprzeda�y netto ze stawk� obni�on� czwart� - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
			} else if (czySwiadczenieTurystyczne==true ||czySprzedazSztuki==true) {
				faktura.setP136(testBigDec); 	//Suma warto�ci sprzeda�y netto ze stawk� 0%
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true"
				faktura.setP137(testBigDec); 	//Suma warto�ci sprzeda�y zwolnionej
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true"
			}
			faktura.setP15(/*listaDokDoJpkFa.get(i).get*/new BigDecimal(222)); //Kwota nale�no�ci og�em
			faktura.setP16(czyMetodaKasowa); 	// W przypadku dostawy towar�w lub �wiadczenia us�ug, w odniesieniu do kt�rych obowi�zek podatkowy powstaje zgodnie z art. 19a ust. 5 pkt 1 lub art. 21 ust. 1
												//wyrazy "metoda kasowa", nale�y poda� warto�� "true"; w przeciwnym przypadku - warto�� - "false"
			faktura.setP17(czySamofakturowanie); //faktur, o kt�rych mowa w art. 106d ust. 1 -
												//wyraz "samofakturowanie",nale�y poda� warto�� "true"; w przeciwnym przypadku - warto�� - "false"
			faktura.setP18(vatRozliczaNabywca); //W przypadku dostawy towar�w lub wykonania us�ugi, dla kt�rych obowi�zanym do rozliczenia podatku, podatku VAT jest nabywca
												// wyrazy "odwrotne obci��enie", nale�y poda� warto�� "true"; w przeciwnym przypadku - warto�� - "false"
			if(czyJestZwolnienieZVat==true) {
				faktura.setP19(czyJestZwolnienieZVat); 	//W przypadku dostawy towar�w lub �wiadczenia us�ug zwolnionych od podatku na podstawie art. 43 ust. 1, art. 113 ust. 1 i 9 albo przepis�w wydanych na podstawie art. 82 ust. 3
														//nale�y poda� warto�� "true"; w przeciwnym przypadku - warto�� - "false"
				faktura.setP19A(/*listaDokDoJpkFa.get(i).get*/"Podstawa prawna zwolnienia");
				faktura.setP19B(/*listaDokDoJpkFa.get(i).get*/"Wskazanie dyrektywy zwalniaj�cej");//nale�y wskaza� przepis dyrektywy 2006/112/WE
				faktura.setP19C(/*listaDokDoJpkFa.get(i).get*/"Inna podstawa prawna zwalniaj�ca");
			} else if (czyFakturaWierzyciela==true) {
				faktura.setP20(czyFakturaWierzyciela); //W przypadku, o kt�rym mowa w art. 106c ustawy nale�y poda� warto�� "true"; w przeciwnym przypadku - warto�� - "false"
				faktura.setP20A(/*listaDokDoJpkFa.get(i).get*/"Nazwa organu egzekucyjnego");//nale�y poda� nazw� organu egzekucyjnego lub imi� i nazwisko komornika
				faktura.setP20B(/*listaDokDoJpkFa.get(i).get*/"Nazwa organu egzekucyjnego2");//nale�y poda� nazw� organu egzekucyjnego lub imi� i nazwisko komornika
			} else if (czyFakturaPrzedstawiciela==true) {
				faktura.setP21(czyFakturaPrzedstawiciela); //Je�li faktura wystawiona przez jego przedstawiciela podatkowego 
				faktura.setP21A(/*listaDokDoJpkFa.get(i).get*/"Dane przedstawiciela podatkowego");
			} else if (czyFakturaPodatnikaDwa==true) {
				faktura.setP23(czyFakturaPodatnikaDwa); //W przypadku faktur wystawianych przez drugiego w kolejno�ci podatnika
			} else if (czySwiadczenieTurystyczne==true) {
				faktura.setP106E2(czySwiadczenieTurystyczne); // w przypadku �wiadcze� us�ug turystycznych
			} else if (czySprzedazSztuki==true) {
				faktura.setP106E3(czySprzedazSztuki); // w przypadku dostaw towar�w u�ywanych lub dzie� sztuki
			}
			
		}
			
	
		faktura.setRodzajFaktury("rodzaj faktury");// VAT(podstawowa) KOREKTA ZAL(zaliczkowa) POZ(pozosta�e)
		faktura.setPrzyczynaKorekty("przyczyna korekty");
		faktura.setNrFaKorygowanej("nr faktury korygowanej");
		faktura.setOkresFaKorygowanej("okres faktury koryguj�cej");
		faktura.setZALZaplata(new BigDecimal(555));//otrzymana kwoty zaliczki
		faktura.setZALPodatek(new BigDecimal(4321));//kwota podatku dla faktury zaliczkowej

		
		}
		return listaFaktur;
	}


	private XMLGregorianCalendar mapToGregDate(long date)
	{
		try
		{
			gregDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd").format(date));
		} catch (DatatypeConfigurationException e)
		{
			e.printStackTrace();
		}
		return gregDate;
	}
}
