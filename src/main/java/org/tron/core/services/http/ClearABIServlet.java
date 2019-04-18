package org.tron.core.services.http;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.capsule.utils.TransactionUtil;
import org.tron.protos.Contract.ClearABIContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;

import static org.tron.core.services.http.Util.getVisiblePost;

@Component
@Slf4j(topic = "API")
public class ClearABIServlet extends HttpServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String contract = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(contract);
      boolean visible = getVisiblePost( contract );
      ClearABIContract.Builder build = ClearABIContract.newBuilder();
      JsonFormat.merge(contract, build, visible );
      Transaction tx = wallet
          .createTransactionCapsule(build.build(), ContractType.ClearABIContract)
          .getInstance();
      JSONObject jsonObject = JSONObject.parseObject(contract);
      if (jsonObject.containsKey(Constant.DELAY_SECONDS)) {
        long delaySeconds = jsonObject.getLong(Constant.DELAY_SECONDS);
        tx = TransactionUtil.setTransactionDelaySeconds(tx, delaySeconds);
      }
      response.getWriter().println(Util.printTransaction(tx, visible ));
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(Util.printErrorMsg(e));
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }
}
