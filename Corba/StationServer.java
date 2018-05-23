import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.Properties;
import java.util.*;
import java.io.*;

// Класс, реализующий IDL-интерфейс базовой станции
class StationServant extends StationPOA {
  // Вместо представленных ниже двух переменных здесь
  // должен быть список пар "номер - объектная ссылка"
    Map<String, TubeCallback> tubeNumToRefsMap = new HashMap<>();  
  TubeCallback tubeRef;
  String tubeNum;
  Cell.ServerDB dbRef;
  String stationName;

  // Метод регистрации трубки в базовой станции
  public int register (TubeCallback objRef, String phoneNum) {
      tubeNumToRefsMap.put(phoneNum, objRef);
      dbRef.registerPhoneNumber(stationName, phoneNum);
      //tubeNum = phoneNum;
     System.out.println("Станция: зарегистрирована трубка "+phoneNum);
     return (1);
  }

  public int sendSMSToDB(String fromNum, String toNum, String message) {
	System.out.println("Station: tube "+ fromNum + " send message to DB to number " + toNum);
	dbRef.sendSMS(fromNum, toNum, message);
	return 1;
    }
  // Метод пересылки сообщения от трубки к трубке
  public int sendSMSToTube (String fromNum, String toNum, String message) {
    System.out.println("Станция: трубка "+fromNum+" посылает сообщение "+toNum);
    // Здесь должен быть поиск объектной ссылки по номеру toNum
    if(tubeNumToRefsMap.get(toNum) == null){
	System.out.println("Nomer ne suchestvuet!");
	tubeNumToRefsMap.get(fromNum).sendSMS(fromNum, "Nomer ne suchestvuet!");
    } else {
	tubeNumToRefsMap.get(toNum).sendSMS(fromNum, message);
    }
    return (1);
  }
    
  public int setServerDBRef(Cell.ServerDB serverDBRef){
	dbRef = serverDBRef;
	return 1;
  }

  public int setStationName(String stationName){
	this.stationName = stationName;
	return 1;
  }
  };

// Класс, реализующий сервер базовой станции
public class StationServer {

  public static void main(String args[]) {
    try{
	BufferedReader inpt  = new BufferedReader(new InputStreamReader(System.in));
	System.out.println("Vvedite imya etoi stancii");
	String stationName = inpt.readLine();
      // Создание и инициализация ORB
      ORB orb = ORB.init(args, null);

      // Получение ссылки и активирование POAManager
      POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      rootpoa.the_POAManager().activate();

      // Создание серванта для CORBA-объекта "базовая станция" 
      StationServant servant = new StationServant();

      // Получение объектной ссылки на сервант
      org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
      Station sref = StationHelper.narrow(ref);
          
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

      // Связывание объектной ссылки с именем
      NameComponent path[] = ncRef.to_name( stationName );
      ncRef.rebind(path, sref);

      NameComponent dbPath[] = ncRef.to_name("ServerDB");
      Cell.ServerDB dbRef = ServerDBHelper.narrow(ncRef.resolve(dbPath));

      dbRef.registerStation(sref, stationName);

      servant.setServerDBRef(dbRef);
      servant.setStationName(stationName);

      System.out.println("Сервер готов и ждет ...");

      // Ожидание обращений от клиентов (трубок)
      orb.run();
      } 
     catch (Exception e) {
        System.err.println("Ошибка: " + e);
        e.printStackTrace(System.out);
      };
    };
  }; 
