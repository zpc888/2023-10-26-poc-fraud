package com.prot.poc.esign;

import com.prot.poc.esign.vo.SignPackage;
import com.prot.poc.esign.vo.SignPackageResult;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-05T23:55 Sunday
 */
public interface ESignService {
    SignPackageResult sendForSign(SignPackage pkg);
    byte[] downloadDocument(String packageId, String docId);
}
