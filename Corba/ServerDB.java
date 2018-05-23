import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.Properties;
import java.util.*;

// Класс, реализующий IDL-интерфейс базовой станции
class ServerDBServant extends ServerDBPOA {
  // Вместо представленных ниже двух переменных здесь
  // должен быть список пар "номер - объектная ссылка"
    Map<String, Station> tubeNumToStationMap = new HashMap<>();
    Map<String, String> tubeNumToStationName = new HashMap<>();
  Station stationRef;
    String tubeNum;

  // Метод регистрации трубки в базовой станции
    public int registerStation (Station objRef, String stationName) {
      tubeNumToStationMap.put(stationName, objRef);
      System.out.println("Server: station registred " + stationName);
      return (1);
    };

    public int registerPhoneNumber (String stationName, String phoneNumber) {
	tubeNumToStationName.put(phoneNumber, stationName);
      System.out.println("Server: number registered " + phoneNumber + " on station "  + stationName);
      return (1);
    };

  // Метод пересылки сообщения от трубки к трубке
  public int sendSMS (String fromNum, String toNum, String message) {
    System.out.println("Станция: трубка "+fromNum+" посылает сообщение "+toNum);
    // Здесь должен быть поиск объектной ссылки по номеру toNum
    if(tubeNumToStationName.get(toNum) == null){
	System.out.println("Nomer ne suchestvuet!");
	String tempStationName = tubeNumToStationName.get(fromNum);
	tubeNumToStationMap.get(tempStationName).sendSMSToTube(fromNum, fromNum, "Nomer ne suchestvuet!");
    } else {
	String tempStationName = tubeNumToStationName.get(toNum);
	tubeNumToStationMap.get(tempStationName).sendSMSToTube(fromNum, toNum, message);
    }
    return (1);
    };
  };

// Класс, реализующий сервер базовой станции
public class ServerDB {

  public static void main(String args[]) {
    try{
      // Создание и инициализация ORB
      ORB orb = ORB.init(args, null);

      // Получение ссылки и активирование POAManager
      POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      rootpoa.the_POAManager().activate();

      // Создание серванта для CORBA-объекта "базовая станция" 
      ServerDBServant servant = new ServerDBServant();

      // Получение объектной ссылки на сервант
      org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
      Cell.ServerDB sref = ServerDBHelper.narrow(ref);
          
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

      // Связывание объектной ссылки с именем
      String name = "ServerDB";
      NameComponent path[] = ncRef.to_name( name );
      ncRef.rebind(path, sref);

      System.out.println("Database server ready and wait...");

      // Ожидание обращений от клиентов
      orb.run();
      } 
     catch (Exception e) {
        System.err.println("Ошибка: " + e);
        e.printStackTrace(System.out);
      };
    };
  }; 
