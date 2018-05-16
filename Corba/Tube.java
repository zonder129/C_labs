import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.io.*;

// Класс вызова телефонной трубки
class TubeCallbackServant extends TubeCallbackPOA {
 String myNum;	// Номер трубки

 // Конструктор класса
 TubeCallbackServant (String num) {
   myNum = num;
   };

 // Метод обработки принятого сообщения
 public int sendSMS(String fromNum, String message) {
    System.out.println(myNum+": принято сообщение от "+fromNum+": "+message);
    return (0);
    };
 
 // Метод, возвращающий номер трубки
 public String getNum() {
    return (myNum);
    };
  };

// Класс, используемый для создания потока управления
class ORBThread extends Thread {
  ORB myOrb;

  // Конструктор класса
  ORBThread(ORB orb) {
    myOrb = orb;
    };

   // Метод запуска потока
   public void run() {
     myOrb.run();
     };
  };
 
// Класс, имитирующий телефонную трубку
public class Tube {

  public static void main(String args[]) {
    try {
      String myNum = "1234";	// Номер трубки
      // Создание и инициализация ORB
      ORB orb = ORB.init(args, null);

      //Создание серванта для IDL-интерфейса TubeCallback
      POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      rootPOA.the_POAManager().activate();
      TubeCallbackServant listener  = new TubeCallbackServant(myNum);
      rootPOA.activate_object(listener);
      // Получение ссылки на сервант
      TubeCallback ref = TubeCallbackHelper.narrow(rootPOA.servant_to_reference(listener));
      
      // Получение контекста именования
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
      // Преобразование имени базовой станции в объектную ссылку
      NameComponent nc = new NameComponent("BaseStation", "");
      NameComponent path[] = {nc};
      Station stationRef = StationHelper.narrow(ncRef.resolve(path));

      // Регистрация трубки в базовой станции
      stationRef .register(ref, myNum);
      System.out.println("Трубка зарегистрирована базовой станцией");

      // Запуск ORB в отдельном потоке управления
      // для прослушивания вызовов трубки
      ORBThread orbThr = new ORBThread(orb);
      orbThr.start();

      // Бесконечный цикл чтения строк с клавиатуры и отсылки их
      // базовой станции
      BufferedReader inpt  = new BufferedReader(new InputStreamReader(System.in));
      String msg;
      while (true) {
        msg = inpt.readLine();
        stationRef .sendSMS(myNum, "7890", msg);
        // Обратите внимание: номер получателя 7890 в описанной ранее
        // реализации базовой станции роли не играет
        }

      } catch (Exception e) {
	 e.printStackTrace();
      };


    };

  };