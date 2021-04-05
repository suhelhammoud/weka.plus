import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.pas.PasUtils;
import weka.gui.explorer.Explorer;


public class AppLauncher {
  static Logger logger = LoggerFactory.getLogger(AppLauncher.class.getName());

  public static void main(String[] args) {
    logger.info("App started");
    Explorer.main(args);
  }
}
