package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.InputGoogleAuthToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.openqa.selenium.WebElement;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.INPUT_LOCATOR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.INPUT_VALUE;

@Slf4j
@ExecutorForClass(InputGoogleAuthToken.class)
public class InputGoogleAuthTokenExecutor extends AbstractUiExecutor<InputGoogleAuthToken> {

	private static final String HMAC_ALGORITHM = "HmacSHA1";
	private static final String SIX_DIGITS_FORMAT = "%06d";

	public InputGoogleAuthTokenExecutor(ExecutorDependencies dependencies) {
		super(dependencies);
	}

	@Override
	public void execute(final InputGoogleAuthToken o, final CommandResult result) {
		result.put(INPUT_LOCATOR, o.getLocator());
		WebElement webElement = UiUtil.findWebElement(dependencies, o.getLocator(), o.getLocatorStrategy());
		UiUtil.waitForElementVisibility(dependencies, webElement);
		String value = UiUtil.resolveSendKeysType(generateCode(o.getSecretKey()), webElement, dependencies.getFile());
		result.put(INPUT_VALUE, value);
		log.info(VALUE_LOG, value);
		webElement.sendKeys(value);
		UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
	}

	private String generateCode(String secretKey) {
		try {
			long timeStep = 30L;
			long counter = Instant.now().getEpochSecond() / timeStep;

			Base32 base32 = new Base32();
			byte[] key = base32.decode(secretKey.toUpperCase().getBytes(StandardCharsets.UTF_8));

			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
			byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());

			int offset = hash[hash.length - 1] & 0xF;
			int binary =
					((hash[offset] & 0x7F) << 24) |
							((hash[offset + 1] & 0xFF) << 16) |
							((hash[offset + 2] & 0xFF) << 8) |
							(hash[offset + 3] & 0xFF);

			int otp = binary % 1_000_000;
			return String.format(SIX_DIGITS_FORMAT, otp);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
