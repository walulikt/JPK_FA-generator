

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jpk.JPK;
import jpk.MSCountryCodeType;
import jpk.ObjectFactory;
import jpk.TAdresPolski;
import jpk.TIdentyfikatorOsobyNiefizycznej;

public class MenuActionGenerujJpkFa 
{

	private String typFaktury = ""; //cztery opcje do wyboru zmachowaæ z baza
	private final MSCountryCodeType DEF_SEL_VAT_EU_PREF = MSCountryCodeType.PL;

	//-------------------ustawiæ weryfikacjê kto jest wystawc¹?

	/*protected JasperPrintUtils jpu;
	ReadOnlyRecord r ;
	public ReadOnlyRecord getCurrentRecord()
	{
		if (r != null)
			return r;
		return jpu.getCurrentRecord();
	}
	String filia = ModulSwitch.MS_FILIA ? FiliaHelper.getCurrenBranchCode(getCurrentRecord()) : "";*/

	//-------------------------------

	@Override
	public void run()
	{
		SystemLog log = new SystemLog();
		log.start(SystemLog.SYSTEM_ACTION_TYPE_CUSTOM_BLOCK_OP);
		JPK_FA_Gen_Manger jpkGenMan= new JPK_FA_Gen_Manger();
		Transaction tr = null;
		DokumentyDAO dokDao = null;
		DaneFiFiliiDAO daneFilii=null;
		JAXBContext jaxbContext = null;
		Marshaller jaxbMarshaller = null;

		byte celZlozenia = 01;
		byte wariantFormularza = 01;
/*	!*/	String typFaktur = null; //dla ka¿dego rodzaju faktur nale¿y wygenerowaæ oddzielne JPK_FA (mo¿e ustawic ¿eby generowaly sie wszystkie cztery rodzaje JPK_FA?)		

		try
		{
			boolean isOk = true;
			tr = Application.app.getDefaultBaseNode().createTransaction();
			dokDao=new DokumentyDAO(tr);
			daneFilii=new DaneFiFiliiDAO(tr); //NAZWA tabeli w której s¹ dane klienta
			ReadOnlyRecord r = RecordEditWindow.runRecordEditWithParams(tr, mp, "EDIT_OD_DO_JPK_FA", true);
			String filia = FiliaHelper.getCurrenBranchCode(r);
			ReadOnlyRecord pdgRecord = Qconst.getPodmiotGospByFilia(filia);

			if (r == null)
				return;

			long dataOd = r.getLong("DATAOD");
			long dataDo = r.getLong("DATADO");
			List<Dokumenty> listaDokJpkFa = dokDao.findByDataSprzedazy(dataOd, dataDo);


			ObjectFactory oFactory = new ObjectFactory();
			JPK jpkFA = oFactory.createJPK();

			//-------------------------------------nag³ówek

			jpkFA.setNaglowek(jpkGenMan.createReadyJPKNaglowek(dataOd, dataDo));

			//-----------------------------------------------Podmiot1
			JPK.Podmiot1 podmiot = oFactory.createJPKPodmiot1();

			String nip = pdgRecord.getString(FLD_COMPANY_NIP);
			String nazwaSprzedawcy = pdgRecord.getString(FLD_COMPANY_NAME);
			String regon = pdgRecord.getString(FLD_COMPANY_REGON);
			String woj="";
			String pow="";
			String gm="";
			String ul = "";
			String nrDomu="";
			String nrLokalu="";
			String miejscowosc = pdgRecord.getString(FLD_COMPANY_CITY);
			String kod = pdgRecord.getString(FLD_COMPANY_ZIP_CODE);
			String poczta = pdgRecord.getString(FLD_COMPANY_CITY);

			TIdentyfikatorOsobyNiefizycznej identyfikator = jpkGenMan.createReadyIdentyfikatorOsNiefiz(nip, nazwaSprzedawcy, regon);
			TAdresPolski adres =jpkGenMan.createReadyAdresPol(woj, pow, gm, ul, nrDomu, nrLokalu, miejscowosc, kod, poczta);

			podmiot.setIdentyfikatorPodmiotu(identyfikator);
			podmiot.setAdresPodmiotu(adres);

			jpkFA.setPodmiot1(podmiot);

			//--------------------------------Faktura
			JPK.Faktura faktura = oFactory.createJPKFaktura();


			jpkFA.getFaktura().add(faktura);

			//-----------------------Faktura liczba kontrolna

			JPK.FakturaCtrl fakturaCtrl = oFactory.createJPKFakturaCtrl();
			fakturaCtrl.setLiczbaFaktur(new BigInteger("9"));
			fakturaCtrl.setWartoscFaktur(new BigDecimal(777)); //£¹czna wartoœæ kwot brutto faktur w okresie, którego dotyczy JPK_FA

			jpkFA.setFakturaCtrl(fakturaCtrl);

			//-----------------------Zestawienie stawek podatku w okresie którego dotyczy JPK_FA

			JPK.StawkiPodatku stawkiPod = oFactory.createJPKStawkiPodatku();
			stawkiPod.setStawka1(new BigDecimal(666)); //Wartoœæ stawki podstawowej
			stawkiPod.setStawka2(new BigDecimal(666)); //Wartoœæ stawki obni¿onej pierwszej
			stawkiPod.setStawka3(new BigDecimal(666)); //Wartoœæ stawki obni¿onej drugiej
			stawkiPod.setStawka4(new BigDecimal(666)); //Wartoœæ stawki obni¿onej trzeciej - pole rezerwowe
			stawkiPod.setStawka5(new BigDecimal(666)); //Wartoœæ stawki obni¿onej czwartej - pole rezerwowe

			jpkFA.setStawkiPodatku(stawkiPod);

			//------------------------Faktura Wiersze

			JPK.FakturaWiersz wiersz = oFactory.createJPKFakturaWiersz();
			wiersz.setP2B("nr faktury");
			wiersz.setP7("Przedmiot sprzeda¿y"); //Nazwa (rodzaj) towaru lub us³ugi
			wiersz.setP8A("jednostka przedmiotu sprzeda¿y"); //Miara dostarczonych towarów lub zakres wykonanych us³ug
			wiersz.setP8B(new BigDecimal(818)); //Iloœæ (liczba) dostarczonych towarów lub zakres wykonanych us³ug
			wiersz.setP9A(new BigDecimal(787)); //Cena jednostkowa towaru lub us³ugi bez kwoty podatku (cena jednostkowa netto)
			wiersz.setP9B(new BigDecimal(2)); //W przypadku zastosowania art.106e ustawy, cena wraz z kwot¹ podatku (cena jednostkowa brutto)
			wiersz.setP10(new BigDecimal(11)); //Kwoty wszelkich opustów lub obni¿ek cen, w tym w formie rabatu z tytu³u wczeœniejszej zap³aty,
												//pole opcjonalne gdy przynajmniej jedno z pól P_106E_2 i P_106E_3  = "true")
			wiersz.setP11(new BigDecimal(33)); //Wartoœæ dostarczonych towarów lub wykonanych us³ug, bez kwoty podatku (wartoœæ sprzeda¿y netto)
												//Pole opcjonalne gdy przynajmniej jedno z pól P_106E_2 i P_106E_3 = "true")
			wiersz.setP11A(new BigDecimal(99)); //W przypadku zastosowania art. 106e ust.7 i 8 ustawy, wartoœæ sprzeda¿y brutto
			wiersz.setP12("Stawka podatku"); //max length 2. tylko wartoœci: 23 22 8 7 5 3 0 zw
			//wiersz.setTyp("czy siê ustawia na G?");		//

			jpkFA.getFakturaWiersz().add(wiersz);

			//--------------------------Faktura Wiersze liczba kontrolna
			JPK.FakturaWierszCtrl wierszeCtrl = oFactory.createJPKFakturaWierszCtrl();
			wierszeCtrl.setLiczbaWierszyFaktur(new BigInteger("5")); //Liczba wierszy faktur, w okresie którego dotyczy JPK_FA
			wierszeCtrl.setWartoscWierszyFaktur(new BigDecimal(33)); //£¹czna wartoœæ kolumny P_11 tabeli FakturaWiersz w okresie, którego dotyczy JPK_FA

			jpkFA.setFakturaWierszCtrl(wierszeCtrl);


			try
			{
				jaxbContext = JAXBContext.newInstance(JPK.class);
				jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			} catch (JAXBException e)
			{
				e.printStackTrace();
			}
			jaxbMarshaller.marshal(jpkFA, new File("E:\\Home\\twalulik\\JPK_FA.xml"));

			XML_validator validator = new XML_validator();
			String xmlPath = "E:\\Home\\twalulik\\JPK_FA.xml";
			String xsdPath = "./Schemat_JPK_FA(1)_v1-0.xsd";
			try
			{
				validator.validate(xmlPath, xsdPath);
			} catch (ValidationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			Application.app.messageBox(mp, "Q-Line 3000", "Wyst¹pi³ b³¹d podczas generowania pliku: " + e.toString(),
					MessageWindow.STYLE_CANCEL);
		} finally
		{
			log.stop();
			if (tr != null && !tr.isDisposed())
				tr.dispose();
		}
	}

}
