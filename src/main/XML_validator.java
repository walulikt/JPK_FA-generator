package main;
import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

public class XML_validator {
       
        public boolean validate(String xmlPath, String xsdPath) throws ValidationException{
        	
        	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                Schema schema = schemaFactory.newSchema(new File(xsdPath));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new File(xmlPath)));
                return true;
            } catch (SAXException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        
}