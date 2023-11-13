package com.prot.poc.esign;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-05T13:35 Sunday
 */
public interface PkiResolver {
    byte[] resolvePrivateKey();
    byte[] resolvePublicKey();
}
