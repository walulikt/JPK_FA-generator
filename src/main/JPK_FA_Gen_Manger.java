

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
		naglowek.setKodUrzedu("Wstawiæ kod urzêdu Skarbowego");
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
	/*!*/	boolean vatRozliczaNabywca = false;			//czy ustawiæ pole?
	/*!*/	boolean czySwiadczenieTurystyczne = false;
	/*!*/	boolean czySprzedazSztuki = false;
	/*!*/   BigDecimal testBigDec = new BigDecimal(1);
	/*!*/ 	boolean czyMetodaKasowa = false; 			//ustawiæ pole. True jeœli klient mia³ sprzeda¿ powy¿ej 1,2mln Euro i zg³osi³
	/*!*/ 	boolean czySamofakturowanie = false;		//czy ustawiæ pole? klient jest nabywc¹, ale wystawia fakturê sprzeda¿owa 
	/*!*/ 	boolean	czyJestZwolnienieZVat = false;		//czy ustawiæ pole?
	/*!*/	boolean czyFakturaWierzyciela = false; 		//ustawic pole, zrobic na sztywno, zlozyc ze dana z bazy?
	/*!*/	boolean czyFakturaPrzedstawiciela = false;
	/*!*/	boolean czyFakturaPodatnikaDwa = false;
	
		for (int i = 0; i < listaDokDoJpkFa.size(); i++)
		{
			if (listaDokDoJpkFa.get(i).getTypd() == typFaktury) //zlikwidowane zbêdne for
		{

			long dataWystawienia = listaDokDoJpkFa.get(i).getDatawyst().getDayNumber();//sprawdziæ czy getDayNumber == miliseconds from
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
			faktura.setP6(mapToGregDate(dataSprzedazy));						//data sprzeda¿y/otrzymania zap³aty
			faktura.setP131(/*listaDokDoJpkFa.get(i).get*/new BigDecimal(101)); //Suma wartoœci sprzeda¿y netto ze stawk¹ podstawow¹ - aktualnie 23% albo 22%
			faktura.setP141(/*listaDokDoJpkFa.get(i).get*/new BigDecimal(32)); 	//Kwota podatku od sumy wartoœci sprzeda¿y netto ze stawk¹ podstawow¹ - aktualnie 23% albo 22%
			if(vatRozliczaNabywca==true || czySwiadczenieTurystyczne==true ||czySprzedazSztuki==true ) {
				faktura.setP132(testBigDec); 	//Suma wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ - 8 % albo 7%
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP142(testBigDec); 	//Kwota podatku od sumy wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ - 8 % albo 7%
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP133(testBigDec); 	//Suma wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ drug¹ - 5%
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP143(testBigDec); 	//Kwota podatku od sumy wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ drug¹ - 5%
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP134(testBigDec); 	//Suma wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ trzeci¹ - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP144(testBigDec); 	//Kwota podatku od sumy wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ trzeci¹ - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP135(testBigDec); 	//Suma wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ czwart¹ - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true" lub pole P_18 = "true"
				faktura.setP145(testBigDec); 	//Kwota podatku od sumy wartoœci sprzeda¿y netto ze stawk¹ obni¿on¹ czwart¹ - pole rezerwowe
												//Opcjonalnie gdy pole P_106E_2 i P_106E_3 = "true" lub pole P_18 = "true"
			} else if (czySwiadczenieTurystyczne==true ||czySprzedazSztuki==true) {
				faktura.setP136(testBigDec); 	//Suma wartoœci sprzeda¿y netto ze stawk¹ 0%
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true"
				faktura.setP137(testBigDec); 	//Suma wartoœci sprzeda¿y zwolnionej
												//Opcjonalnie gdy pole P_106E_3 i P_106E_3 = "true"
			}
			faktura.setP15(/*listaDokDoJpkFa.get(i).get*/new BigDecimal(222)); //Kwota nale¿noœci ogó³em
			faktura.setP16(czyMetodaKasowa); 	// W przypadku dostawy towarów lub œwiadczenia us³ug, w odniesieniu do których obowi¹zek podatkowy powstaje zgodnie z art. 19a ust. 5 pkt 1 lub art. 21 ust. 1
												//wyrazy "metoda kasowa", nale¿y podaæ wartoœæ "true"; w przeciwnym przypadku - wartoœæ - "false"
			faktura.setP17(czySamofakturowanie); //faktur, o których mowa w art. 106d ust. 1 -
												//wyraz "samofakturowanie",nale¿y podaæ wartoœæ "true"; w przeciwnym przypadku - wartoœæ - "false"
			faktura.setP18(vatRozliczaNabywca); //W przypadku dostawy towarów lub wykonania us³ugi, dla których obowi¹zanym do rozliczenia podatku, podatku VAT jest nabywca
												// wyrazy "odwrotne obci¹¿enie", nale¿y podaæ wartoœæ "true"; w przeciwnym przypadku - wartoœæ - "false"
			if(czyJestZwolnienieZVat==true) {
				faktura.setP19(czyJestZwolnienieZVat); 	//W przypadku dostawy towarów lub œwiadczenia us³ug zwolnionych od podatku na podstawie art. 43 ust. 1, art. 113 ust. 1 i 9 albo przepisów wydanych na podstawie art. 82 ust. 3
														//nale¿y podaæ wartoœæ "true"; w przeciwnym przypadku - wartoœæ - "false"
				faktura.setP19A(/*listaDokDoJpkFa.get(i).get*/"Podstawa prawna zwolnienia");
				faktura.setP19B(/*listaDokDoJpkFa.get(i).get*/"Wskazanie dyrektywy zwalniaj¹cej");//nale¿y wskazaæ przepis dyrektywy 2006/112/WE
				faktura.setP19C(/*listaDokDoJpkFa.get(i).get*/"Inna podstawa prawna zwalniaj¹ca");
			} else if (czyFakturaWierzyciela==true) {
				faktura.setP20(czyFakturaWierzyciela); //W przypadku, o którym mowa w art. 106c ustawy nale¿y podaæ wartoœæ "true"; w przeciwnym przypadku - wartoœæ - "false"
				faktura.setP20A(/*listaDokDoJpkFa.get(i).get*/"Nazwa organu egzekucyjnego");//nale¿y podaæ nazwê organu egzekucyjnego lub imiê i nazwisko komornika
				faktura.setP20B(/*listaDokDoJpkFa.get(i).get*/"Nazwa organu egzekucyjnego2");//nale¿y podaæ nazwê organu egzekucyjnego lub imiê i nazwisko komornika
			} else if (czyFakturaPrzedstawiciela==true) {
				faktura.setP21(czyFakturaPrzedstawiciela); //Jeœli faktura wystawiona przez jego przedstawiciela podatkowego 
				faktura.setP21A(/*listaDokDoJpkFa.get(i).get*/"Dane przedstawiciela podatkowego");
			} else if (czyFakturaPodatnikaDwa==true) {
				faktura.setP23(czyFakturaPodatnikaDwa); //W przypadku faktur wystawianych przez drugiego w kolejnoœci podatnika
			} else if (czySwiadczenieTurystyczne==true) {
				faktura.setP106E2(czySwiadczenieTurystyczne); // w przypadku œwiadczeñ us³ug turystycznych
			} else if (czySprzedazSztuki==true) {
				faktura.setP106E3(czySprzedazSztuki); // w przypadku dostaw towarów u¿ywanych lub dzie³ sztuki
			}
			
		}
			
	
		faktura.setRodzajFaktury("rodzaj faktury");// VAT(podstawowa) KOREKTA ZAL(zaliczkowa) POZ(pozosta³e)
		faktura.setPrzyczynaKorekty("przyczyna korekty");
		faktura.setNrFaKorygowanej("nr faktury korygowanej");
		faktura.setOkresFaKorygowanej("okres faktury koryguj¹cej");
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
