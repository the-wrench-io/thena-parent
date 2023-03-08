package io.resys.thena.docdb.spi;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.annotation.Nullable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import io.resys.thena.docdb.api.exceptions.RepoException;


public final class OidUtils {

  private OidUtils() { }

  private static final SecureRandom SECURE_RANDOM;

  static {
    SecureRandom secureRandom;
    try {
      secureRandom = SecureRandom.getInstance("NativePRNGNonBlocking");
    }
    catch (NoSuchAlgorithmException e) {
      try {
        secureRandom = SecureRandom.getInstanceStrong();
      }
      catch (NoSuchAlgorithmException e1) {
        secureRandom = new SecureRandom();
      }
    }
    SECURE_RANDOM = secureRandom;
  }

  public static byte[] generateOID() {
    byte[] oid = new byte[16];
    SECURE_RANDOM.nextBytes(oid);
    return oid;
  }

  public static String toString(byte[] oid) {
    return Hex.encodeHexString(oid);
  }
  
  public static String gen() {
    return toString(generateOID());
  }

  @Nullable
  public static byte[] toOID(@Nullable String id) {
    if (id == null) {
      return null;
    }
    try {
      id = id.replace("-", "");
      byte[] oidBytes = Hex.decodeHex(id.toCharArray());
      if (oidBytes.length > 16) {
        throw new RepoException(id + " is too long ID");
      }
      if (oidBytes.length < 16) {
        oidBytes = Arrays.copyOf(oidBytes, 16);
      }
      return oidBytes;
    } catch (DecoderException e) {
      throw new RepoException(id + " is not valid ID: " + e.getMessage());
    }
  }

  @Nullable
  static Integer validateRevValue(@Nullable String rev) {
    if (rev == null) {
      return null;
    }
    if (StringUtils.isBlank(rev) || !StringUtils.isNumeric(rev)) {
      throw new IllegalArgumentException("rev must be numeric");
    }
    return Integer.parseInt(rev);
  }
}
