package rt;

import com.corba.Coverage.Inner.Point;
import com.corba.Coverage.Inner.PointHolder;
import com.corba.Coverage.Item;
import com.corba.Coverage.ItemServicePOA;
import com.corba.Coverage.Status;
import com.corba.Coverage.StatusHolder;
import com.corba.Coverage.StatusSeqHolder;
import com.corba.Coverage.Tag;
import com.corba.Coverage.TagSeqHolder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.StringHolder;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/// Swagger確認用のCORBAサーバー。ItemServiceとClockServiceを公開し、IORを<dir>/item.ior・clock.iorに書く。
/// 2つ目の引数にポートを渡すと、そのポートでNameServiceも公開し、`ItemService`・`ClockService`の名前で登録する
/// （`corbaname::localhost:<port>#ItemService`で引ける）
public class Server {
  static ORB orb;

  /// 実サーバーと同じく、SJISのバイト列をISO-8859-1の文字列にして返す
  static String sjis(String s) {
    return new String(s.getBytes(Charset.forName("Windows-31J")), StandardCharsets.ISO_8859_1);
  }

  static class Clock extends com.corba.Other.ClockServicePOA {
    public long now() {
      return System.currentTimeMillis();
    }

    public String zone(Tag hint) {
      return "JST";
    }
  }

  static class Items extends ItemServicePOA {
    public Item getItem(int id) {
      Any extra = orb.create_any();
      extra.insert_string(sjis("おまけ"));
      return new Item(
          id,
          sjis("商品") + id,
          1980.5,
          true,
          extra,
          new int[] {1, 2, 3},
          new String[] {sjis("ラベル")},
          new Tag[] {new Tag("color", sjis("赤"))},
          new String[] {"alias"},
          new Point(10, 20),
          Status.ACTIVE);
    }

    public void putItem(Item item) {}

    public Tag[] listTags(String prefix) {
      return new Tag[] {new Tag(prefix + "1", "v1"), new Tag(prefix + "2", "v2")};
    }

    public String[] echoNames(String[] names) {
      return names;
    }

    public String greet(String name) {
      return sjis("こんにちは、") + name;
    }

    public Any echoAny(Any value) {
      return value;
    }

    public int findItems(String keyword, TagSeqHolder tags, StringHolder message) {
      tags.value = new Tag[] {new Tag("keyword", keyword)};
      message.value = sjis("1件見つかりました");
      return 1;
    }

    public void touch(IntHolder counter) {
      counter.value++;
    }

    public void relabel(StringHolder label, PointHolder p) {
      label.value = label.value + "!";
      p.value.x += 10;
    }

    public boolean ping() {
      return true;
    }

    public String[] globalNames(String[] names) {
      return names;
    }

    public Point movePoint(Point p, PointHolder previous) {
      previous.value = new Point(p.x, p.y);
      return new Point(p.x + 1, p.y + 1);
    }

    public void outAny(AnyHolder value) {
      value.value = orb.create_any();
      value.value.insert_long(42);
    }

    public Status setStatus(int id, Status status, StatusSeqHolder history) {
      history.value = new Status[] {Status.ACTIVE, status};
      return status;
    }

    public void cycleStatus(StatusHolder status) {
      status.value = Status.from_int((status.value.value() + 1) % 3);
    }
  }

  public static void main(String[] args) throws Exception {
    Properties props = new Properties();
    if (args.length > 1) {
      props.setProperty("com.sun.corba.ee.ORBServerPort", args[1]);
    }
    orb = ORB.init(new String[0], props);
    POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    poa.the_POAManager().activate();
    Path dir = Path.of(args[0]);
    org.omg.CORBA.Object clock = poa.servant_to_reference(new Clock());
    org.omg.CORBA.Object item = poa.servant_to_reference(new Items());
    Files.writeString(dir.resolve("clock.ior"), orb.object_to_string(clock));
    Files.writeString(dir.resolve("item.ior"), orb.object_to_string(item));
    if (args.length > 1) {
      new com.sun.corba.ee.impl.naming.cosnaming.TransientNameService(
          (com.sun.corba.ee.spi.orb.ORB) orb);
      NamingContextExt naming =
          NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
      naming.rebind(naming.to_name("ItemService"), item);
      naming.rebind(naming.to_name("ClockService"), clock);
    }
    orb.run();
  }
}
