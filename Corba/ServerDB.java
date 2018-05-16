import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.Properties;
import java.util.*;

// Класс, реализующий IDL-интерфейс базовой станции
class StationServant extends StationPOA {
  // Вместо представленных ниже двух переменных здесь
  // должен быть список пар "номер - объектная ссылка"
    Map<String, TubeCallback> tubeNumToRefsMap = new HashMap<String, TubeCallback>();  
  TubeCallback tubeRef;
  String tubeNum;

  // Метод регистрации трубки в базовой станции
  public int register (TubeCallback objRef, String phoneNum) {
      tubeNumToRefsMap.put(phoneNum, objRef);
     
      //tubeNum = phoneNum;
     System.out.println("Станция: зарегистрирована трубка "+phoneNum);
     return (1);
     };

  // Метод пересылки сообщения от трубки к трубке
  public int sendSMS (String fromNum, String toNum, String message) {
    System.out.println("Станция: трубка "+fromNum+" посылает сообщение "+toNum);
    // Здесь должен быть поиск объектной ссылки по номеру toNum
    if(tubeNumToRefsMap.get(toNum) == null){
	System.out.println("Nomer ne suchestvuet!");
	tubeNumToRefsMap.get(fromNum).sendSMS(fromNum, "Nomer ne suchestvuet!");
    } else {
	tubeNumToRefsMap.get(toNum).sendSMS(fromNum, message);
    }
    return (1);
    };
  };

// Класс, реализующий сервер базовой станции
public class StationServer {

  public static void main(String args[]) {
    try{
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
      String name = "BaseStation";
      NameComponent path[] = ncRef.to_name( name );
      ncRef.rebind(path, sref);

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
