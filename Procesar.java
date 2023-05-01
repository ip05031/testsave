package com.ba.doc.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.logging.log4j.Logger;  
import org.apache.logging.log4j.LogManager;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ba.doc.contratos.ACISimplificada;
import com.ba.doc.contratos.CartaBeneficiario;
import com.ba.doc.contratos.CartaInformativa;
import com.ba.doc.contratos.ContratoDepositos;
import com.ba.doc.contratos.ContratoMarco;

import com.ba.doc.content.ContentManagerOracle;
import com.ba.doc.correo.DocCorreoMDBBean;
import com.ba.doc.correo.EnviarLatinia;
import com.ba.doc.dto.BancoDTO;
import com.ba.doc.dto.ClienteDTO;
import com.ba.doc.dto.ContratoDTO;
import com.ba.doc.dto.ProcesoDTO;
import com.ba.doc.dto.TablaInteresDTO;
import com.ba.doc.mantiz.Mantiz;
import com.ba.doc.oracle.EnviarOracle;


import com.ba.doc.util.ConectorFtp;
import com.ba.doc.util.Configuracion;
import com.ba.doc.util.MQ;
import com.ba.doc.util.Prueba;
import com.ba.doc.util.Utils;
import com.ba.doc.util.Validacion;
import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.jcraft.jsch.JSchException;



public class Procesar {
	
	/**
	 * @param args
	 */
	
	public  static Logger  log 					= LogManager.getLogger(Procesar.class);
	public  static Logger  traza 				= LogManager.getLogger("traza");
	public  static Logger  trama 				= LogManager.getLogger("loggerTrama");
	
	private static String  ipMq 				= "";
	private static String  channelMq 			= "";
	private static String  managerMq			= "";
	private static String  reqMq 				= "";
	private static String  rspMq 				= "";
	private static String  rspMq2 				= "";
	
	private static int	   port 				= 0;
	private static int 	   timeout 				= 0;

	// PROPIEDADES PARA FTP
	private static String FTPUSER 				= null;
	private static String FTPPASS 				= null;
	private static String FTPIP					= null;
	private static int FTPPUERTO 				= 0;
	
	private static String ruta_local 			= "";
	private static String ruta_remota  			= "";
	
	private static String url_copia 			= "";
	private static String url_firmas 			= "";
	private static String url_original 			= "";
	private static String url_mail 				= "";
	private static String url_imagenes			= "";
	
	
	private static String url_certif			= "";
	private static String contra_certif 		= "";
	
	private static String hilo0					= "";
	
	// Valores Desa
	private static String url_desa_docs 		= "/home/ebanca/sv/jornadasMDB/ahorro/contratos/";
	private static String url_desa_remoto		= "/root/omnicanal/contratos/ahorro/";
	private static String url_desa_cert 		= "/home/ebanca/sv/jornadasMDB/documentos/mlopez2.p12";
	private static String url_desa_img 			= "/home/ebanca/sv/jornadasMDB/ahorro/imagenes/";
	
	private static String url_desa_pwd 			= "";
	
	
	private static String url_desa_ftp_ip		= "10.55.150.49";
	private static String url_desa_ftp_us		= "root";
	private static String url_desa_ftp_ct 		= "";	
	private static int 	  url_desa_ftp_pt		= 22;
	 
	private static String url_desa_mq_ip		= "10.209.249.196";
	private static String url_desa_mq_mg		= "BAMQGWDES";
	private static String url_desa_mq_ch		= "CH.SERVER.CONN"; 
	private static String url_desa_mq_qr		= "CONTRATOSAHORRO.REQ.CL";
	private static String url_desa_mq_qs		= "CONTRATOSAHORRO.RSP.CL";
	private static String url_desa_mq_ts		= "CONTRATO.SCAN.TEST.CL";
	private static int 	  url_desa_mq_pt		= 1417;
	private static int 	  url_desa_mq_to		= 30000;
	private static Configuracion conf 			= new Configuracion();
	
	/********************************************************* Redirigiendo ***********************************************************/
	public Procesar() {
		
	}
	
	public static void main(String[] args) {

		
	}
	
	
	public static String redirigir(String Mensaje,byte[] correlationId,String hilo){
		
		log.info(hilo + " MDB -> REDIRIGIR " );
		hilo0 					= hilo;
		long TInicio 			= 0;
		long TFin 				= 0;
		long TResp				= 0;
		long fin 				= 0;
		TInicio 				= System.currentTimeMillis();
		String respuesta 		= null;
		int codigo 		 		= 0;
		Date fec				= new Date();
		boolean enviadoMQ		= false;
		SimpleDateFormat sdf1	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		long inicio 			= System.currentTimeMillis();
		CrearTodo all 			= new CrearTodo();	
		ProcesoDTO proceso		= new ProcesoDTO();
		Notificacion notif		= new Notificacion();
		
		//String[] parametros 	= Utils.parametros(Mensaje, "", hilo);
		
		proceso.setHilo(hilo);
		proceso.setHoraInicio();
		proceso.setearMQ();
		proceso.setTrama(Mensaje);
		
		
		log.info  ( "      "+hilo+" ------------" + proceso.getHoraInicio() + "------------");
		log.info  ( "      "+hilo+ "------------Trama: ["+proceso.getTrama().trim()+"]" );
		trama.info( "      "+hilo+ "------------Trama: ["+proceso.getTrama().trim()+"]" );
		
		log.info(   hilo + " MDB -> REDIRIGIR -> CODIGO TRAMA: ["+proceso.getCodigoContNum()+"]" );
		
		switch (proceso.getCodigoContNum()) {	    
		
			case 20		: respuesta = all.CrearTodos(proceso, hilo);															break;		// Creacion de contratos cancelacion y cancelacion express
			case 21     : respuesta = notif.cancelacionExpress(proceso);														break;		// Notificaion de cancelacion express
			case 30		: respuesta = all.CrearTodos(proceso, hilo);															break;
			case 40     : respuesta = all.CrearTodos(proceso, hilo);	                                        				break;
			case 50     : respuesta = all.CrearTodos(proceso, hilo);	                                        				break;
			case 46		: respuesta = all.CrearDeclaracion(proceso, hilo);														break;
			case 51 	: respuesta = crearContrato(Mensaje, correlationId, hilo ,"FORMULARIO VINCULACION"); 					break;
			case 70		: respuesta = notificacionDeCuentaCreada(Mensaje, correlationId, hilo, "NOTIFICACION DE CUENTA CREADA");break;
			
			case 47     : respuesta = respuestaQuemadaFormV();                                                  				break;
			case 71 	: respuesta= all.envioCorreosNotificaciones(proceso);													break;
			case 72 	: respuesta= "";																						break;
			case 73 	: respuesta= "";																						break;			
			default 	: respuesta = respuestaGenerica(codigo);																break;
			
		}			
		 
			fin = System.currentTimeMillis();
			log.info(hilo + " MDB -> REDIRIGIR -> RESPUESTA : ["+respuesta+"]" );
			byte[] test = null;
			if( proceso.getCodigoContrato().equals("00")){
				test = MQ.sendMQ(url_desa_mq_ip, url_desa_mq_ch  , url_desa_mq_mg  , proceso.getPort(), url_desa_mq_ts , respuesta, correlationId,hilo);
			}
			else{
				test = MQ.sendMQ(proceso.getIpMq(), proceso.getChannelMq(), proceso.getManagerMq(), proceso.getPort(), proceso.getRspMq(), respuesta, correlationId,hilo);
			}
			
			log.info(hilo + " MDB -> REDIRIGIR -> SENT MQ  CORE : ["+test+"]" );
			enviadoMQ = true;


		TFin  = System.currentTimeMillis();
		TResp = TFin - TInicio;
		log.info(hilo + " MDB -> REDIRIGIR -> TIEMPO FINAL : ["+TResp+"]" );
		double tiempoFinal = (double) ((fin - inicio)/1000);
		Date fec2 = new Date();
		log.info( hilo+" ------------" +sdf1.format(fec2) + "------------");
		log.info( hilo+" ------------ TIEMPO FINAL ------------------------");
		log.info( hilo+" TIEMPO : " + tiempoFinal);
		return respuesta;
	}

	/********************************************************* Redirigiendo ***************************************************************/
	public static ConectorFtp conectarFTP(){
		log.info(hilo0 + " Iniciando FTP");
		ConectorFtp ftp 	= null;
		

		try {
			ftp = new ConectorFtp(FTPUSER,FTPPASS,FTPIP,FTPPUERTO,hilo0);
			log.info(hilo0 + " FTP Iniciado con éxito");
		} 
		catch (Exception e) {
			log.info(hilo0 +  " Error al iniciar el FTP");
			ftp = null;
		}
		return ftp;
		
	}	
	
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static byte[] sendMQ(String host, String channel, String mqManager,int port, String cola, String mensaje, byte[] correlationId) {
		//System.out.println("host [" + host + "], channel [" + channel 	+ "], mqManager [" + mqManager + "], port [" + port	+ "], cola [" + cola + "], mensaje [" + mensaje + "]");	
		MQQueue queue = null;
		MQQueueManager queueManager = null;
		MQMessage outMsg = null;
		byte[] resultado = null;

		try {

			MQEnvironment.hostname 	= host;
			MQEnvironment.channel 	= channel;
			MQEnvironment.port 		= port;
			queueManager 			= new MQQueueManager(mqManager);
			queue 					= queueManager.accessQueue(cola, MQC.MQOO_FAIL_IF_QUIESCING | MQC.MQOO_OUTPUT, null, null, null);
			outMsg 					= new MQMessage();
			outMsg.format 			= MQC.MQFMT_STRING;
			outMsg.messageFlags 	= MQC.MQMT_REQUEST;
			if (correlationId != null) {
				outMsg.correlationId = correlationId;
			}
			outMsg.writeString(mensaje);
			MQPutMessageOptions pmo = new MQPutMessageOptions();
			pmo.options = MQC.MQPMO_NEW_MSG_ID;
			pmo.options += MQC.MQPMO_SYNCPOINT;
			queue.put(outMsg, pmo);
			queueManager.commit();
		} catch (MQException ex) {
			ex.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (queue != null)
					queue.close();
			} catch (Exception e) {
			}
			try {
				if (queueManager != null)
					queueManager.disconnect();
			} catch (Exception e) {
			}
			try {
				if (queueManager != null)
					queueManager.close();
			} catch (Exception e) {
			}
		}

		resultado = correlationId;
		if (resultado == null) {
			resultado = outMsg.messageId;
		}

		return resultado;
	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String respuestaGenerica(int codigo) {
		String respuesta = respuestaError( "000001" , "NO EXISTE ESE CONTRATO ["+codigo+"]" );
		log.info("respuesta gen : "+ respuesta);
		return respuesta;
	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String respuestaError(String codigo , String mensaje){
		String respuesta = "";
		respuesta = rellenarEspacios(codigo ,6 ) + rellenarEspacios(mensaje , 100 );
		return respuesta;
	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String respuestaExitoTdodos(String codigo , String descripcion,String cuenta , String unico, String codProd , String fecha , String lista){
		String respuesta = "";
		
		respuesta += rellenarEspacios(codigo ,6 );
		respuesta += rellenarEspacios(descripcion ,100 );
		respuesta += rellenarEspacios(cuenta ,12 );
		respuesta += rellenarEspacios(unico ,9 );
		respuesta += rellenarEspacios(codProd ,9 );
		respuesta += rellenarEspacios(fecha ,10 );
		respuesta += rellenarEspacios(lista , 190);

		return respuesta;
	}
	
	public static String respuestaErrorTodos(String codigo , String descripcion){
		String respuesta = "";
		
		respuesta += rellenarEspacios(codigo ,6 );
		respuesta += rellenarEspacios(descripcion ,100 );
		respuesta += rellenarEspacios("----" ,12 );
		respuesta += rellenarEspacios("----" ,9 );
		respuesta += rellenarEspacios("----" ,9 );
		respuesta += rellenarEspacios("----" ,10 );
		
		respuesta += rellenarEspacios("41" ,2 );
		respuesta += rellenarEspacios("N" ,1 );
		respuesta += rellenarEspacios("----" ,20 );
		respuesta += rellenarEspacios("----" ,15 );
		
		respuesta += rellenarEspacios("42" ,2 );
		respuesta += rellenarEspacios("N" ,1 );
		respuesta += rellenarEspacios("----" ,20 );
		respuesta += rellenarEspacios("----" ,15 );
		
		respuesta += rellenarEspacios("43" ,2 );
		respuesta += rellenarEspacios("N" ,1 );
		respuesta += rellenarEspacios("----" ,20 );
		respuesta += rellenarEspacios("----" ,15 );
		
		respuesta += rellenarEspacios("44" ,2 );
		respuesta += rellenarEspacios("N" ,1 );
		respuesta += rellenarEspacios("----" ,20 );
		respuesta += rellenarEspacios("----" ,15 );
		
		respuesta += rellenarEspacios("45" ,2 );
		respuesta += rellenarEspacios("N" ,1 );
		respuesta += rellenarEspacios("----" ,20 );
		respuesta += rellenarEspacios("----" ,15 );
		
		return respuesta;
	}
	
	
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static void manipulatePdf2(String src, String dest , String hilo , ContratoDTO contrato) {
		String urlImg = contrato.getUrlImgs();
		
		log.info(hilo + " MARCA COPIA ["+src+"] OUT["+dest+"]");
		try {
			PdfReader reader 	= new PdfReader(src);
			int n 				= reader.getNumberOfPages();
			PdfStamper stamper 	= new PdfStamper(reader, new FileOutputStream(dest));
			stamper.setRotateContents(false);
			Font f 				= new Font(FontFamily.HELVETICA, 40);
			Phrase p 			= new Phrase("                     Copia Cliente", f);
			Image img 			= Image.getInstance(urlImg+"logo_BA01.gif");
			float w 			= img.getScaledWidth();
			float h 			= img.getScaledHeight();
			PdfGState gs1 		= new PdfGState();
			gs1.setFillOpacity(0.13f);
			PdfContentByte over;
			Rectangle pagesize;
			float x, y;
			for (int i = 1; i <= n; i++) {
				pagesize = reader.getPageSize(i);
				x = (pagesize.getLeft() + pagesize.getRight()) / 2;
				y = (pagesize.getTop() + pagesize.getBottom()) / 2;
				over = stamper.getOverContent(i);
				over.saveState();
				over.setGState(gs1);
				ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, 300, 350,-300);
				over.restoreState();
			}
			stamper.close();
			reader.close();
			contrato.setCopiado(true);
		}
		catch (IOException eio){
			eio.printStackTrace();
			log.error(hilo + "ERROR IO WM:["+ eio.getMessage()  +"]");			
		}
		catch ( DocumentException ed){
			ed.printStackTrace();
			log.error(hilo + "ERROR DOC WM ["+ ed.getMessage()  +"]");
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.error(hilo + "ERROR GRAL WM ["+ e.getMessage()  +"]");
		}

	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static void imprimirTrama (String trama , int partes , String hilo){
    	int dividendo = trama.length();
    	int divisor = partes;
    	int cociente 	 = ( dividendo / divisor);
    	int comprobar = cociente * divisor;
    	int residuo =dividendo - comprobar ;
    	int inicio = 0;
    	int fin = 0;
    	for ( int i = 0 ; i <  cociente ; i++ ){
    		inicio 	=  i * (divisor);
    		fin = inicio + divisor;
    		log.info(hilo + " TRAMA        |" + trama.substring(inicio,fin) + "|" );

    	}
    	if  ( residuo > 0 ){
    		inicio 	= fin;
    		fin = inicio + residuo;
    		log.info(hilo + " TRAMA        |" + trama.substring(inicio,fin) + "|" );
    	}
	}
	
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String respuestaExitosa(String codRsp, String dscRsp, String codSec, String numCta, String numCte , String prdTip, String fechad, String codCMO, String dirFil, String namFil){
		String respuesta = ""+
		"" + rellenarEspacios( codRsp , 6 )+
		"" + rellenarEspacios( dscRsp , 100 )+
		"" + rellenarEspacios( codSec , 20 )+
		"" + rellenarEspacios( numCta , 12 )+
		"" + rellenarEspacios( numCte , 9 )+
		"" + rellenarEspacios( prdTip , 9 )+
		"" + rellenarEspacios( fechad , 10 )+
		"" + rellenarEspacios( codCMO , 50 )+
		"" + rellenarEspacios( dirFil , 60 )+
		"" + rellenarEspacios( namFil , 40 );
		
		log.info("Respueta exitosa" + respuesta);
		return respuesta;
	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String respuestaExitosaQuemada(){
        String respuesta = ""+
        "" + rellenarEspacios( "000000" , 6 )+
        "" + rellenarEspacios( "FINALIZADO CON ÉXITO" , 100 )+
        "" + rellenarEspacios( "003111614549" , 12 )+
        "" + rellenarEspacios( "004017233" , 9 )+
        "" + rellenarEspacios( "XXXXXXXXX" , 9 )+
        "" + rellenarEspacios( "22-06-2020" , 10 )+
        "" + rellenarEspacios( "41" , 2 )+
        "" + rellenarEspacios( "Y" , 1 )+
        "" + rellenarEspacios( "BE21300424042020" , 20 )+
        "" + rellenarEspacios( "417248" , 15 )+
        "" + rellenarEspacios( "42" , 2 )+
        "" + rellenarEspacios( "Y" , 1 )+
        "" + rellenarEspacios( "CI21300424042020" , 20 )+
        "" + rellenarEspacios( "417249" , 15 )+
        "" + rellenarEspacios( "43" , 2 )+
        "" + rellenarEspacios( "N" , 1 )+
        "" + rellenarEspacios( "0" , 20 )+
        "" + rellenarEspacios( "0" , 15 )+
        "" + rellenarEspacios( "44" , 2 )+
        "" + rellenarEspacios( "Y" , 1 )+
        "" + rellenarEspacios( "AC21300424042020" , 20 )+
        "" + rellenarEspacios( "417250" , 15 )+
        "" + rellenarEspacios( "45" , 2 )+
        "" + rellenarEspacios( "N" , 1 )+
        "" + rellenarEspacios( "CD21300424042020" , 20 )+
        "" + rellenarEspacios( "0" , 15 )+
        "" + rellenarEspacios( "46" , 2 )+
        "" + rellenarEspacios( "Y" , 1 )+
        "" + rellenarEspacios( "CD21300424042020" , 20 )+
        "" + rellenarEspacios( "417251" , 15 );
       
       
        log.info("Respueta exitosa" + respuesta);
        return respuesta;
    }
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String rellenarEspacios(String text , int longitud){
		String respuesta = "";
		int length = text.length();
		int n = longitud - length;
		if ( n > 0) {
			for (int i = 0; i < n; i++) {
				text += " ";
			}
			respuesta = text;
		}
		else if(n < 0){
			respuesta += text.substring(0,longitud);
		}
		else{
			respuesta = text;
		}
		return respuesta;
	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	
	public static boolean crearArchivos(File file, ContratoDTO contrato, String hilo){
		boolean respuesta = false;
		try {
			String docName 			= contrato.getNombreOriginal();
			File outFile 			= new File( contrato.getUrlOriginal() + docName);
			FileInputStream in 		= new FileInputStream(file);
			FileOutputStream out	= new FileOutputStream(outFile);
			String ambiente 		= conf.getParamCMO("cmo.gral.ambiente");
			
			int c;
			while ((c = in.read()) != -1)
				out.write(c);
			in.close();
			out.close();
			log.info(hilo + " MDB -> CONTRATO COPIADO");
			
			String DESTF 	= contrato.getUrlFirma() + "FIRMADO_" + docName + "";
			String SRC 		= contrato.getUrlOriginal() + docName;
			String DESTM 	= contrato.getUrlCopy() + "COPIA_" + docName + "";
			
			if (contrato.getFirmarDoc().equals("Y")) {
				Prueba.firmarPDF(SRC, DESTF, contrato, hilo);
				contrato.setNombreFirmado("FIRMADO_" + docName + "");
			}
			
			manipulatePdf2(SRC, DESTM , hilo ,contrato);
			contrato.setNombreCopia( "COPIA_" + docName + "" );
			return true;
		} catch (IOException e) {
			log.error("Hubo un error de entrada/salida!!!");
			e.printStackTrace();
			contrato.setCopiado(false);
			contrato.setFirmado(false);
			return false;
		} catch (Exception e) {
			log.error("Hubo un error General Ex");
			e.printStackTrace();
			contrato.setCopiado(false);
			contrato.setFirmado(false);			
			return false;
		}
	}

	/*--------------------------------------------------------------------------------------------------------------------*/
	
	public static String crearTodoContratos(String Mensaje,byte[] correlationId,String hilo ,String tipo ){
		return "";
	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String crearContrato(String Mensaje,byte[] correlationId,String hilo ,String tipo ){
		
		String respuesta 		= "";
		boolean validaciones 	= false;
		//String []parametros 	= Mensaje.split("!");
		String[] parametros = Utils.parametros(Mensaje, "", hilo);
		
		
		int codigo 				= Integer.parseInt( parametros[0] );
		ClienteDTO cliente 		= new ClienteDTO();
		BancoDTO banco 			= new BancoDTO();
		ContratoDTO contrato	= new ContratoDTO();
		TablaInteresDTO tblInt 	= new TablaInteresDTO();
		Validacion validar		= new Validacion();
		DocCorreoMDBBean correo = new DocCorreoMDBBean();
		ContentManagerOracle cmo= new ContentManagerOracle();
		EnviarLatinia latiniaSrv= new EnviarLatinia(); 	
		EnviarOracle oracle 	= new EnviarOracle();
		boolean archivosCreados = false;
		boolean sendCopyCMO 	= false;
		boolean sendOrigCMO		= false;
		boolean sendEmail		= false;
		String nombreDoc 		= null;
		String codDoc 			= "";
		log.info(hilo + " MDB -> CREAR_CONTRATO " );
		log.info(hilo + " MDB -> CREAR_CONTRATO -> VALIDANDO INPUT " );
		
		Map<Integer, String> docAhorro = Utils.nombreDocumentos();
		
		switch (codigo) {
			case 41		: validaciones = validar.validarBeneficiario(		Mensaje, cliente, banco, contrato , hilo); 			break;
			case 42		: validaciones = validar.validarCartaInformativa(	Mensaje, cliente, banco, contrato , tblInt ,hilo); 	break;
			case 43		: validaciones = validar.validarContratoMarco(		Mensaje, cliente, banco, contrato , hilo); 			break;
			case 44		: validaciones = validar.validarACISimplificada(	Mensaje, cliente, banco, contrato , hilo); 			break;
			case 45		: validaciones = validar.validarContratoDeposito(	Mensaje, cliente, banco, contrato , tblInt , hilo); break;
			default 	: validaciones = false;	break;
		}	
		contrato.setCodigoContrato(""+codigo);
		if ( validaciones && contrato.isValidado() ){
			log.info(hilo + " MDB -> CREAR_CONTRATO -> CAMBIANDO ESTADO ORACLE" );
			oracle.cambiarEstadoOracle( cliente, contrato, hilo );
			log.info(hilo + " MDB -> CREAR_CONTRATO -> CREANDO CONTRATOS " );
			File contratoFile		= null;
			switch (codigo) {
				case 41		: contratoFile 	= CartaBeneficiario.CartaBeneficiarios(Mensaje,contrato.getSeguridad(),hilo,contrato,cliente,banco); 				break;
				case 42		: contratoFile 	= CartaInformativa.cartaInfo(cliente,banco,contrato,tblInt,hilo); 					break;
				case 43		: contratoFile 	= ContratoMarco.ContratoMarcoOperacionesBancarias(cliente,banco, contrato,hilo); 	break;
				case 44		: contratoFile 	= ACISimplificada.ContratoACI( cliente, contrato,hilo); 							break;
				case 45		: contratoFile 	= ContratoDepositos.contratoDepositos(cliente, banco, contrato,tblInt,hilo); 		break;
			}
			
			if(cliente.getAlmacenarCM().equals("N")){
				contrato.setFirmarDoc("N");
				if (contratoFile != null && contratoFile.exists() && contrato.isCreado()) {
					log.info(hilo + " MDB -> CREAR_CONTRATO -> CREANDO ARCHIVOS" );
					archivosCreados = crearArchivos(contratoFile , contrato , hilo);
					
					String SFTP_IP   = conf.getParamCMO("sftp.contratos.no.firmados.ip");
					String SFTP_USER = conf.getParamCMO("sftp.contratos.no.firmados.user");	
					String SFTP_PASS = conf.getParamCMO("sftp.contratos.no.firmados.pass");
					int SFTP_PORT = Integer.parseInt(conf.getParamCMO("sftp.contratos.no.firmados.port"));
					String SFTP_REMOTE_PATH = conf.getParamCMO("sftp.contratos.no.firmados.remote.path");
					
					ConectorFtp sftp = new ConectorFtp(SFTP_USER, SFTP_PASS, SFTP_IP, SFTP_PORT,hilo);
					String destino = contrato.getUrlOriginal() + contrato.getNombreCopia();
					
					try {
						if(sftp.put(SFTP_REMOTE_PATH, destino, cliente.getNumeroUnico() + docAhorro.get(codigo))){
							System.out.println("Archivo copiado en SFTP remoto: " + cliente.getNumeroUnico() + docAhorro.get(codigo));
							log.info("Archivo copiado en SFTP remoto: " + cliente.getNumeroUnico() + docAhorro.get(codigo));
							
							File file_org = new File(url_original + contrato.getNombreOriginal());
							file_org.delete();
							file_org = new File(url_copia + contrato.getNombreCopia());
							file_org.delete();
							
							respuesta = Procesar.respuestaExitosa(
									"000000",
									"FINALIZADO EXITOSAMENTE",
									contrato.getSeguridad(),
									cliente.getNumCuenta(),
									cliente.getNumCliente(),
									contrato.getCodProdTipoProd(),
									cliente.getFechaHora(),
									contrato.getCodigoCMO(),
									SFTP_REMOTE_PATH,
									cliente.getNumeroUnico() + docAhorro.get(codigo));
							log.info("Respuesta proceso: " + respuesta);
						} else{
							System.err.println("Error copiando en SFTP remoto, archivo a guardar: " + cliente.getNumeroUnico() + docAhorro.get(codigo));
							log.info("Error copiando en SFTP remoto, archivo a guardar: " + cliente.getNumeroUnico() + docAhorro.get(codigo));
							
							respuesta = Procesar.respuestaError("000010","Error copiando archivo sin firmas en SFTP remoto");
						}
					} catch (JSchException e) {
						System.err.println("Error copiando en SFTP remoto, archivo a guardar: "+cliente.getNumeroUnico() + docAhorro.get(codigo));
						log.info("Error copiando en SFTP remoto, archivo a guardar: "+cliente.getNumeroUnico() + docAhorro.get(codigo));
						
						respuesta = Procesar.respuestaError("000010","Error copiando archivo sin firmas en SFTP remoto");
						e.printStackTrace();
					}
				} else {
					respuesta = respuestaError("000004", "ERROR CREANDO PDF - " + tipo);
				}
			} else {
				if ( contratoFile != null && contratoFile.exists() && contrato.isCreado() ) {
					log.info(hilo + " MDB -> CREAR_CONTRATO -> CREANDO ARCHIVOS [ORIGINAL , COPIA , FIRMA ] " );
					archivosCreados = crearArchivos( contratoFile , contrato , hilo);
					
					if ( archivosCreados && contrato.isFirmado() && contrato.isCopiado() ){	
						log.info(hilo + " MDB -> CREAR_CONTRATO -> SUBIENDO COPIA AL CMO  " );
						sendCopyCMO = cmo.enviarContentOra(url_copia,contrato,cliente, true,hilo);
						
						if(sendCopyCMO && contrato.isEnviadoCopiaCMO()){
							log.info(hilo + " MDB -> CREAR_CONTRATO -> SUBIENDO ORIGINAL AL CMO  " );
							sendOrigCMO = cmo.enviarContentOra(url_firmas,contrato,cliente, false,hilo); 
								if( sendOrigCMO && contrato.isEnviadoOriginalCMO()  ){
									log.info(hilo + " MDB -> CREAR_CONTRATO -> ENVIANDO COPIA AL CLIENTE  " );
									sendEmail = latiniaSrv.enviarCorreo(cliente, contrato,hilo);
									if ( sendEmail && contrato.isEnviadoCorreo()){
										respuesta = respuestaExitosa("000000", "FINALIZADO EXITOSAMENTE", contrato.getSeguridad(), cliente.getNumCuenta(), cliente.getNumCliente(), contrato.getCodProdTipoProd(), cliente.getFechaHora() , contrato.getCodigoCMO(), url_copia, contrato.getNombreCopia());
										log.info(hilo + " MDB -> CREAR_CONTRATO -> FIN DEL PROCESO" );
										
										File file_org = new File( url_original + contrato.getNombreOriginal()  );
										file_org.delete();
										file_org = new File( url_copia + contrato.getNombreCopia()  );
										file_org.delete();
										file_org = new File( url_firmas + contrato.getNombreFirmado()  );
										file_org.delete();
										
									} else {
										respuesta = respuestaError("000008", "ERROR ENVIANDO CORREO - "+tipo);
									}
							} else {
								respuesta = respuestaError("000007", "ERROR GUARDANDO EN CONTENT ORACLE ORIGINAL -  - "+tipo);
							}	
						} else {
							respuesta = respuestaError("000006", "ERROR GUARDANDO EN CONTENT ORACLE COPIA -  - "+tipo);
						}
					} else {
						respuesta = respuestaError("000005", "ERROR CREANDO CERTIFICADO Y FIRMA - "+tipo);
					}
				} else {
					respuesta = respuestaError("000004", "ERROR CREANDO PDF - " + tipo);
				}
			}
			
			/*if ( contratoFile != null && contratoFile.exists() && contrato.isCreado() ) {
				log.info(hilo + " MDB -> CREAR_CONTRATO -> CREANDO ARCHIVOS [ORIGINAL , COPIA , FIRMA ] " );
				archivosCreados = crearArchivos( contratoFile , contrato , hilo);
				
				if ( archivosCreados && contrato.isFirmado() && contrato.isCopiado() ){	
					log.info(hilo + " MDB -> CREAR_CONTRATO -> SUBIENDO COPIA AL CMO  " );
					sendCopyCMO = cmo.enviarContentOra(url_copia,contrato,cliente, true,hilo);
					

					
					if(sendCopyCMO && contrato.isEnviadoCopiaCMO()){
						log.info(hilo + " MDB -> CREAR_CONTRATO -> SUBIENDO ORIGINAL AL CMO  " );
						sendOrigCMO = cmo.enviarContentOra(url_firmas,contrato,cliente, false,hilo); 
							if( sendOrigCMO && contrato.isEnviadoOriginalCMO()  ){
								log.info(hilo + " MDB -> CREAR_CONTRATO -> ENVIANDO COPIA AL CLIENTE  " );
								sendEmail = latiniaSrv.enviarCorreo(cliente, contrato,hilo);
								if ( sendEmail && contrato.isEnviadoCorreo()){
									respuesta = respuestaExitosa("000000", "FINALIZADO EXITOSAMENTE", contrato.getSeguridad(), cliente.getNumCuenta(), cliente.getNumCliente(), contrato.getCodProdTipoProd(), cliente.getFechaHora() , contrato.getCodigoCMO(), url_copia, contrato.getNombreCopia());
									log.info(hilo + " MDB -> CREAR_CONTRATO -> FIN DEL PROCESO" );
									
									File file_org = new File( url_original + contrato.getNombreOriginal()  );
									file_org.delete();
									file_org = new File( url_copia + contrato.getNombreCopia()  );
									file_org.delete();
									file_org = new File( url_firmas + contrato.getNombreFirmado()  );
									file_org.delete();
									
								}
								else{
									respuesta = respuestaError("000008", "ERROR ENVIANDO CORREO - "+tipo);
								}
						}
						else{
							respuesta = respuestaError("000007", "ERROR GUARDANDO EN CONTENT ORACLE ORIGINAL -  - "+tipo);
						}	
					}
					else{
						respuesta = respuestaError("000006", "ERROR GUARDANDO EN CONTENT ORACLE COPIA -  - "+tipo);
					}
				}
				else{
					respuesta = respuestaError("000005", "ERROR CREANDO CERTIFICADO Y FIRMA - "+tipo);
				}
			}
			else{
				respuesta = respuestaError("000004", "ERROR CREANDO PDF - " + tipo);
			}*/
		} else {
			respuesta = respuestaError("000003", "ERROR VALIDANDO DATOS PARA - " + tipo );
		}
		
		crearResumen(cliente, contrato, hilo, respuesta);

		log.info(respuesta);
		
		return respuesta;
	
	}

	public static void cargarPropiedades(String hilo){
		Configuracion conf 							= new Configuracion();
		conf.cargarPropertiesCMO();
		ipMq 			= conf.getParamCMO("mq.omniMQ.host") 	== null ? url_desa_mq_ip	: conf.getParamCMO("mq.omniMQ.host");
		channelMq 		= conf.getParamCMO("mq.omniMQ.chn") 	== null	? url_desa_mq_ch	: conf.getParamCMO("mq.omniMQ.chn");
		managerMq		= conf.getParamCMO("mq.omniMQ.mng") 	== null ? url_desa_mq_mg	: conf.getParamCMO("mq.omniMQ.mng");
		reqMq 			= conf.getParamCMO("mq.omniMQ.req") 	== null ? url_desa_mq_qr	: conf.getParamCMO("mq.omniMQ.req");
		rspMq 			= conf.getParamCMO("mq.omniMQ.rsp") 	== null ? url_desa_mq_qs	: conf.getParamCMO("mq.omniMQ.rsp"); 
		port 			= conf.getParamCMO("mq.omniMQ.port") 	== null ? url_desa_mq_pt	: Integer.parseInt( conf.getParamCMO("mq.omniMQ.port") );
		timeout			= conf.getParamCMO("mq.omniMQ.timeout") == null ? url_desa_mq_to	: Integer.parseInt( conf.getParamCMO("mq.omniMQ.timeout") );
		FTPUSER 		= conf.getParamCMO("ftp.cred.user") 	== null ? url_desa_ftp_us 	: conf.getParamCMO("ftp.cred.user");
		FTPPASS 		= conf.getParamCMO("ftp.cred.pass") 	== null ? url_desa_ftp_ct	: conf.getParamCMO("ftp.cred.pass");
		FTPIP			= conf.getParamCMO("ftp.cred.ip")   	== null ? url_desa_ftp_ip	: conf.getParamCMO("ftp.cred.ip");
		FTPPUERTO 		= conf.getParamCMO("ftp.cred.port") 	== null ? url_desa_ftp_pt	: Integer.parseInt(conf.getParamCMO("ftp.cred.port"));
		ruta_local 		= conf.getParamCMO("ftp.url.local") 	== null ? url_desa_docs		: conf.getParamCMO("ftp.url.local") ; 
		ruta_remota  	= conf.getParamCMO("ftp.url.remot")  	== null ? url_desa_remoto 	: conf.getParamCMO("ftp.url.remot") ;
		url_copia 		= conf.getParamCMO("url.dir.copia") 	== null ? url_desa_docs		: conf.getParamCMO("url.dir.copia") ; 
		url_firmas  	= conf.getParamCMO("url.dir.firma")  	== null ? url_desa_docs		: conf.getParamCMO("url.dir.firma") ;
		url_original 	= conf.getParamCMO("url.dir.origi") 	== null ? url_desa_docs		: conf.getParamCMO("url.dir.origi") ; 
		url_mail  		= conf.getParamCMO("url.dir.email")  	== null ? url_desa_docs		: conf.getParamCMO("url.dir.email") ;
		url_certif  	= conf.getParamCMO("url.dir.certi")  	== null ? url_desa_cert 	: conf.getParamCMO("url.dir.certi") ;
		url_imagenes 	= conf.getParamCMO("url.dir.img")  		== null ? url_desa_img 		: conf.getParamCMO("url.dir.img") ;
		contra_certif  	= conf.getParamCMO("url.dir.pass")  	== null ? url_desa_pwd		: conf.getParamCMO("url.dir.pass") ;
		
		log.info(hilo + " MDB -> CREAR_CONTRATO -> cargarPropiedades : " );
		log.info(hilo + " PROPS -> [" + ipMq + "]");
		log.info(hilo + " PROPS -> [" + channelMq + "]");
		log.info(hilo + " PROPS -> [" + managerMq + "]");
		log.info(hilo + " PROPS -> [" + reqMq + "]");
		log.info(hilo + " PROPS -> [" + rspMq + "]");
		log.info(hilo + " PROPS -> [" + port + "]");
		log.info(hilo + " PROPS -> [" + timeout + "]");
		log.info(hilo + " PROPS -> [" + ruta_local + "]");
		log.info(hilo + " PROPS -> [" + ruta_remota + "]");
		log.info(hilo + " PROPS -> [" + url_copia + "]");
		log.info(hilo + " PROPS -> [" + url_firmas + "]");
		log.info(hilo + " PROPS -> [" + url_original + "]");
		log.info(hilo + " PROPS -> [" + url_mail + "]");
		log.info(hilo + " PROPS -> [" + url_certif + "]");
		log.info(hilo + " PROPS -> [" + contra_certif.length() + "]");
		
	}
/*--------------------------------------------------------------------------------------------------------------------*/
	
	//Alexis Beltran(JOABELTR_20200625)
	//Enviar correo al cliente si su cuenta es condicionada
	//y nitificar a mantiz que la cuenta ya esta creada.
	public static String notificacionDeCuentaCreada(String Mensaje,byte[] correlationId,String hilo ,String tipo ){
		log.info(hilo + "notificacionDeCuentaCreada");
		
		String respuesta 			= "";
		boolean validaciones 		= false;
		Validacion validar 			= new Validacion();
		EnviarLatinia latiniaSrv	= new EnviarLatinia();
		Mantiz mantiz				= new Mantiz();
		String proceso = "VALINDANDO NOTIFICACION DE CUENTA CONDICIONADA";
		boolean enviarLatinia       = false;
		boolean sendEmail 			= false;
		boolean sendMantiz 			= false;
		boolean accionesEspeciales  = false;
		String indicadorAcciones	= "3";
//		String MensajePrueba = "70!000000000000001!005003260!003111614797!5              !refCuentaCondicionada         !plaCuentaCondicionada         !JOABELTR@BANCOAGRICOLA.COM.SV                                !ROSA CAMELIA                                                ! - Formulario W9   - Carta Waiver  - Formulario W8  - Carta explicativa formulario W8!<documento><nombreDocumento>documento1.pdf</nombreDocumento></documento>";
		
		try{
			
			validaciones = validar.validarNotificacionDeCuentaCondicionada(Mensaje, proceso, hilo);
			log.info(hilo + "NOTIF CTA COND -> VALIDO" + validaciones );
			String[] parametros = Utils.parametros(Mensaje, "", hilo);
			
			
			//Parametros para Mantiz
			String numeroSolicitud		= parametros[1].trim();
			String numeroCliente		= parametros[2].trim();
			String noCuenta 			= parametros[3].trim();
			String combinaciones		= parametros[4].trim();
			//Parametros para Latinia
			String contrato				= parametros[5].trim();
			String plantilla			= parametros[6].trim();
			String correo				= parametros[7].trim();
			String nombreCliente		= parametros[8].trim();
			String listaDinamica		= parametros[9].trim();
			String adjuntos				= parametros[10].trim();
			
			/*srecinos 03/05/2021 Validaciones para proceso de cuenta condicionada F2F
			 * Codigo 1 -> Ejecutar proceso de cuenta condicionada (Mantiz)
			 * Codigo 2 -> Enviar correo por medio de LATINIA
			 * Codigo 3 -> Realizar acciones 1 y 2
			 */
			try {
				indicadorAcciones = parametros[11].trim();
			} catch (Exception e) {
				indicadorAcciones = "3";
			}
			boolean enviarCorreo = true;
			boolean ejecutarProcesoCuentaCondicionada = true;
			if(indicadorAcciones.equals("1")){
				enviarCorreo = false;
				accionesEspeciales = true;
			} else if(indicadorAcciones.equals("2")){
				ejecutarProcesoCuentaCondicionada = false;
				accionesEspeciales = true;
			}
			
			log.info(hilo + " NOTIF CTA COND -> numeroSolicitud 	[" + numeroSolicitud +"]");
			log.info(hilo + " NOTIF CTA COND -> numeroCliente 		[" + numeroCliente  +"]");
			log.info(hilo + " NOTIF CTA COND -> noCuenta			[" + noCuenta  +"]");
			log.info(hilo + " NOTIF CTA COND -> combinaciones		[" + combinaciones  +"]");
			log.info(hilo + " NOTIF CTA COND -> contrato			[" + contrato  +"]");
			log.info(hilo + " NOTIF CTA COND -> plantilla			[" + plantilla  +"]");
			log.info(hilo + " NOTIF CTA COND -> correo				[" + correo  +"]");
			log.info(hilo + " NOTIF CTA COND -> nombreCliente		[" + nombreCliente  +"]");
			
			log.info(hilo + " NOTIF CTA COND -> listaDinamica" + listaDinamica );
			log.info(hilo + " NOTIF CTA COND -> adjuntos" + adjuntos );
			log.info(hilo + " NOTIF CTA COND -> indicadorAcciones " + indicadorAcciones );
			
			
			if(validaciones){
				if(enviarCorreo){
					//1-enviar correo al cliente
					if("0".equalsIgnoreCase(combinaciones.trim())){ // si combinacion es 0, omitir envio
						sendEmail = true;
					}else{
						
						String documentos[] = adjuntos.split(" ");
						String listaDinamicaDocumentosLatinia = "";
						for(int i=0;i<documentos.length;i++){						
							listaDinamicaDocumentosLatinia = listaDinamicaDocumentosLatinia.concat("<documento><nombreDocumento>"+documentos[i].trim()+"</nombreDocumento></documento>");			
						}					
						String parametrosEmail = 
						"<referencia>"+noCuenta+"</referencia>"+		
						"<nombreCliente>"+nombreCliente+"</nombreCliente>"+
						"<docsPaso1>"+listaDinamica+"</docsPaso1>";
						sendEmail = latiniaSrv.enviarCorreo(hilo, contrato,
									plantilla, correo, parametrosEmail, listaDinamicaDocumentosLatinia);
					}	
				}else{
					sendEmail = true;
				}
				
				//2-notificar a mantiz
				if(sendEmail || ejecutarProcesoCuentaCondicionada){			
					
					BigInteger noSolicitud 			= new BigInteger(numeroSolicitud);
					BigInteger noCliente 			= new BigInteger(numeroCliente);
					BigInteger noCuentaNum 			= new BigInteger(noCuenta);					
					
					String BodyRequest = 
							"<numeroSolicitud>"+noSolicitud+"</numeroSolicitud>"+
							"<numeroCuenta>"+noCuentaNum+"</numeroCuenta>"+
							"<numeroUnico>"+noCliente+"</numeroUnico>"+
							"<combinaciones>"+combinaciones+"</combinaciones>";					
					String xmlRequestMantiz = formarPeticionXml("5336000", BodyRequest);
					log.info(hilo + " CTA COND -> REQ MANTIZ " + xmlRequestMantiz);
					
					String respuestaMantiz =  mantiz.obtenerRespuestaMantiz(xmlRequestMantiz, hilo);
					log.info(hilo + " CTA COND -> RESP MANTIZ " + respuestaMantiz);
					
					//obtener codigo de respuesta exitoso;
					DOMParser parser = new DOMParser();
					try {
						parser.parse(new InputSource(new java.io.StringReader(respuestaMantiz)));
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Document doc = parser.getDocument();
					int codigoRsp = -1;
					try {
					 codigoRsp = Integer.parseInt( doc.getElementsByTagName("codigo").item(0).getTextContent()); 
					} catch (Exception e) {
						codigoRsp = -1;
						// TODO: handle exception
					}
					
					if( codigoRsp == 0  ){					
						sendMantiz = true;
					}
					else{
						sendMantiz = false;
					}
				}else if(!ejecutarProcesoCuentaCondicionada){
					sendMantiz = true;
				}
				
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			
			if(accionesEspeciales){
				if(indicadorAcciones.equals("1")){
					if(sendMantiz){
						respuesta = respuestaGenerica("000000", "PROCESO FINALIZADO EXITOSAMENTE");
					}else{
						respuesta = respuestaGenerica("000002", "ERROR AL NOTIFICAR A MANTIZ, VERIFIQUE.");
					}
				} else if(indicadorAcciones.equals("2")){
					if(sendEmail){
						respuesta = respuestaGenerica("000000", "PROCESO FINALIZADO EXITOSAMENTE");
					}else{
						respuesta = respuestaGenerica("000001", "ERROR AL ENVIAR EL CORREO, VERIFIQUE.");
					}
				}
			}else{
				if(sendEmail && sendMantiz){
					respuesta = respuestaGenerica("000000", "PROCESO FINALIZADO EXITOSAMENTE");
				}else if (!sendEmail) {
					respuesta = respuestaGenerica("000001", "ERROR AL ENVIAR EL CORREO, VERIFIQUE.");
				}else if (!sendMantiz) {
					respuesta = respuestaGenerica("000002", "ERROR AL NOTIFICAR A MANTIZ, VERIFIQUE.");
				}else{
					respuesta = respuestaGenerica("000003", "OCURRIO UN ERROR EN EL PROCESO, VERIFIQUE.");
				}	
			}
			
		}
		
		return respuesta;
	}

	/*--------------------------------------------------------------------------------------------------------------------*/
	public static String formarPeticionXml(String idServicio, String Body){
		String respuesta 		= "";
		Date fecha	 			= new Date();
		SimpleDateFormat sdf 	= new SimpleDateFormat("yyyymmddHHmmss");
		try{
			respuesta = 
			"<Request>"+
			"<Header>"+
				"<idServicio>"+idServicio+"</idServicio>"+
				"<idTransaccion>"+sdf.format(fecha)+"</idTransaccion>"+
				"<fechahora>"+sdf.format(fecha)+"</fechahora>"+
				"<idCanal>037011</idCanal>"+
				"<agencia>037</agencia>"+
				"<batch>3711</batch>"+
				"<ipUsuario>0000000127.0.0.1</ipUsuario>"+
				"</Header>"+
			"<Body>"+
				Body+
			"</Body>"+
			"</Request>";
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return respuesta;
	}
	
	
	public static String respuestaGenerica(String codRsp, String dscRsp){
		String respuesta =
		rellenarEspacios( codRsp , 6 ) +
		rellenarEspacios( dscRsp , 100 );
		
		log.info("Respueta Generica " + respuesta);
		return respuesta;
	}
	
	
	
	/*--------------------------------------------------------------------------------------------------------------------*/
	
	//Alexis Beltran(JOABELTR_20200625)
	//Enviar correo al cliente si su cuenta es condicionada
	//y nitificar a mantiz que la cuenta ya esta creada.
	public static String notificacionDeCuentaCread2a(String Mensaje,byte[] correlationId,String hilo ,String tipo ){
		log.info(hilo + "notificacionDeCuentaCreada");
		
		String respuesta 			= "";
		boolean validaciones 		= false;
		Validacion validar 			= new Validacion();
		EnviarLatinia latiniaSrv	= new EnviarLatinia();
		Mantiz mantiz				= new Mantiz();
		String proceso = "VALINDANDO NOTIFICACION DE CUENTA CONDICIONADA";
		boolean enviarLatinia       = false;
		boolean sendEmail 			= false;
		boolean sendMantiz 			= false;
//		String MensajePrueba = "70!000000000000001!005003260!003111614797!5              !refCuentaCondicionada         !plaCuentaCondicionada         !JOABELTR@BANCOAGRICOLA.COM.SV                                !ROSA CAMELIA                                                ! - Formulario W9   - Carta Waiver  - Formulario W8  - Carta explicativa formulario W8!<documento><nombreDocumento>documento1.pdf</nombreDocumento></documento>";
		
		try{
			
			validaciones = validar.validarNotificacionDeCuentaCondicionada(Mensaje, proceso, hilo);
			log.info(hilo + "NOTIF CTA COND -> VALIDO" + validaciones );
			String[] parametros = Utils.parametros(Mensaje, "", hilo);
			
			
			//Parametros para Mantiz
			String numeroSolicitud		= parametros[1].trim();
			String numeroCliente		= parametros[2].trim();
			String noCuenta 			= parametros[3].trim();
			String combinaciones		= parametros[4].trim();
			//Parametros para Latinia
			String contrato				= parametros[5].trim();
			String plantilla			= parametros[6].trim();
			String correo				= parametros[7].trim();
			String nombreCliente		= parametros[8].trim();
			String listaDinamica		= parametros[9].trim();
			String adjuntos				= parametros[10].trim();
			
			log.info(hilo + " NOTIF CTA COND -> numeroSolicitud 	[" + numeroSolicitud +"]");
			log.info(hilo + " NOTIF CTA COND -> numeroCliente 		[" + numeroCliente  +"]");
			log.info(hilo + " NOTIF CTA COND -> noCuenta			[" + noCuenta  +"]");
			log.info(hilo + " NOTIF CTA COND -> combinaciones		[" + combinaciones  +"]");
			log.info(hilo + " NOTIF CTA COND -> contrato			[" + contrato  +"]");
			log.info(hilo + " NOTIF CTA COND -> plantilla			[" + plantilla  +"]");
			log.info(hilo + " NOTIF CTA COND -> correo				[" + correo  +"]");
			log.info(hilo + " NOTIF CTA COND -> nombreCliente		[" + nombreCliente  +"]");
			
			log.info(hilo + " NOTIF CTA COND -> listaDinamica" + listaDinamica );
			log.info(hilo + " NOTIF CTA COND -> adjuntos" + adjuntos );
			
			
			if(validaciones){
				
				//1-enviar correo al cliente
				if("0".equalsIgnoreCase(combinaciones.trim())){ // si combinacion es 0, omitir envio
					sendEmail = true;
				}else{
					
					String documentos[] = adjuntos.split(",");
					String listaDinamicaDocumentosLatinia = "";
					for(int i=0;i<documentos.length;i++){						
						listaDinamicaDocumentosLatinia = listaDinamicaDocumentosLatinia.concat("<documento><nombreDocumento>"+documentos[i].trim()+"</nombreDocumento></documento>");			
					}					
					String parametrosEmail = 
					"<nombreCliente>"+nombreCliente+"</nombreCliente>"+
					"<docsPaso1>"+listaDinamica+"</docsPaso1>";
					sendEmail = latiniaSrv.enviarCorreo(hilo, contrato,
								plantilla, correo, parametrosEmail, listaDinamicaDocumentosLatinia);
				}
				
				//2-notificar a mantiz
				if(sendEmail){			
					
					BigInteger noSolicitud 			= new BigInteger(numeroSolicitud);
					BigInteger noCliente 			= new BigInteger(numeroCliente);
					BigInteger noCuentaNum 			= new BigInteger(noCuenta);					
					
					String BodyRequest = 
							"<numeroSolicitud>"+noSolicitud+"</numeroSolicitud>"+
							"<numeroCuenta>"+noCuentaNum+"</numeroCuenta>"+
							"<numeroUnico>"+noCliente+"</numeroUnico>"+
							"<combinaciones>"+combinaciones+"</combinaciones>";					
					String xmlRequestMantiz = formarPeticionXml("5336000", BodyRequest);
					String respuestaMantiz =  mantiz.obtenerRespuestaMantiz(xmlRequestMantiz, hilo);
					
					log.info(hilo + " CTA COND -> RESP MANTIZ " + respuestaMantiz);
					
					//obtener codigo de respuesta exitoso;
					DOMParser parser = new DOMParser();
					try {
						parser.parse(new InputSource(new java.io.StringReader(respuestaMantiz)));
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Document doc = parser.getDocument();
					int codigoRsp = -1;
					try {
					 codigoRsp = Integer.parseInt( doc.getElementsByTagName("codigo").item(0).getTextContent()); 
					} catch (Exception e) {
						codigoRsp = -1;
						// TODO: handle exception
					}
					
					if( codigoRsp == 0  ){					
						sendMantiz = true;
					}
					else{
						sendMantiz = false;
					}
				}
				
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			
			if(sendEmail && sendMantiz){
				respuesta = respuestaGenerica("000000", "PROCESO FINALIZADO EXITOSAMENTE");
			}else if (!sendEmail) {
				respuesta = respuestaGenerica("000001", "ERROR AL ENVIAR EL CORREO, VERIFIQUE.");
			}else if (!sendMantiz) {
				respuesta = respuestaGenerica("000002", "ERROR AL NOTIFICAR A MANTIZ, VERIFIQUE.");
			}else{
				respuesta = respuestaGenerica("000003", "OCURRIO UN ERROR EN EL PROCESO, VERIFIQUE.");
			}
			
		}
		
		return respuesta;
	}
	/*--------------------------------------------------------------------------------------------------------------------*/
	
	public static String respuestaQuemadaFormV(){
        return respuestaExitosa("000000","FINALIZADO EXITOSAMENTE","FV1653A524032020","003006133715","003360426","0254FORM1","24032020","192860","/home/ebanca/sv/jornadasMDB/ahorro/contratos/","COPIA_Beneficiarios_003360426.pdf");
    }
	
	/*--------------------------------------------------------------------------------------------------------------------*/
	
	public static void crearResumen(ClienteDTO cliente, ContratoDTO contrato , String hilo, String respuesta){

		log.info( hilo+ " ------------ DATOS -------------------------");
		log.info( hilo+ " CODIGO                  :" + contrato.getCodigoContrato() );
		log.info( hilo+ " NUMCLIENTE1             :" + cliente.getNumeroUnico() );
		log.info( hilo+ " NUMCLIENTE2             :" + cliente.getNumCliente()  );
		log.info( hilo+ " NUMCUENTA               :" + cliente.getNumeroCuenta() );
		log.info( hilo+ " NUMCUENTA2              :" + cliente.getNumCuenta() );
		log.info( hilo+ " EMAIL                   :" + cliente.getEmailCliente() );
		log.info( hilo+ " ------------ PROCESO -------------------------");
		log.info( hilo+ " DATOS CORRECTOS         :" + contrato.isValidado() );
		log.info( hilo+ " CODIGO SEGURIDAD        :" + ((contrato.getSeguridad() 			==null) ? "X" : contrato.getSeguridad() ));
		log.info( hilo+ " NOMBRE CONTRATO ORIGINAL:" + ((contrato.getNombreOriginal() 	==null) ? "X" : contrato.getNombreOriginal() ));
		log.info( hilo+ " NOMBRE CONTRATO COPIA   :" + ((contrato.getNombreCopia()    	==null) ? "X" : contrato.getNombreCopia() ));
		log.info( hilo+ " NOMBRE CONTRATO FIRMA   :" + ((contrato.getNombreFirmado()  	==null) ? "X" : contrato.getNombreFirmado() ));
		log.info( hilo+ " X TXT CODDOCUMENTO      :" + ((contrato.getCodigoDocu()  		==null) ? "X" : contrato.getCodigoDocu() ));
		log.info( hilo+ " D SECURITY GROUP        :" + ((contrato.getEntidad()  			==null) ? "X" : contrato.getEntidad() ));
		log.info( hilo+ " D SECURITY GROUP ORG    :" + ((contrato.getEntidadOrg()  		==null) ? "X" : contrato.getEntidadOrg() ));
		log.info( hilo+ " X FECH DOCUMENT         :" + ((cliente.getFechaCMO()  			==null) ? "X" : cliente.getFechaCMO() ));
		log.info( hilo+ " ENVIO CONTRATO COPIA    :" + ((contrato.getCodigoCMO()  		==null) ? "X" : contrato.getCodigoCMO() ));
		log.info( hilo+ " ENVIO CONTRATO FIRMA    :" + ((contrato.getCodigoCMOOrg() 		==null) ? "X" : contrato.getCodigoCMOOrg() ));
		log.info( hilo+ " ------------ RESPUESTA -------------------------");
		log.info( hilo+ " " + respuesta );
	}
	
}