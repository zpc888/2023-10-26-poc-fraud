package com.prot.poc.esign.docusign;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.Configuration;
import com.docusign.esign.client.auth.OAuth;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.prot.poc.common.AppException;
import com.prot.poc.esign.ESignService;
import com.prot.poc.esign.PkiResolver;
import com.prot.poc.esign.vo.SignPackage;
import com.prot.poc.esign.vo.SignPackageResult;
import com.prot.poc.esign.vo.SignPackageStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.prot.poc.esign.docusign.VOAdapter.*;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-05T17:47 Sunday
 */
@Service
@Slf4j
@AllArgsConstructor
public class DocuSignService implements ESignService {
    private final DocuSignConfig docuSignConfig;
    private final PkiResolver pkiResolver;

    @Override
    public SignPackageResult sendForSign(SignPackage pkg) {
        return sendForSign(pkg, null);
    }

    public SignPackageResult sendForSign(SignPackage pkg, String specificUserId) {
        pkg.setEventListenerUrl(docuSignConfig.getEsignEventListenerUrl());
        return withEnvelopesApi((envelopesApi, account) -> {
            EnvelopeDefinition envelopeDef = prepareEnvelopeRequest(pkg);
            envelopeDef.setStatus(SignPackageStatus.sent.name());      // default is created and docs are not email out
            try {
                EnvelopeSummary summary = envelopesApi.createEnvelope(account.getAccountId(), envelopeDef);
                return parseEnvelopeResponse(summary);
            } catch (Exception ex) {
               final String errMsg = "Fail to create envelope";
               log.error(errMsg, ex);
               throw new AppException(errMsg, ex);
            }
        }, specificUserId);
    }

    @Override
    public byte[] downloadDocument(String packageId, String docId) {
        return downloadDocument(packageId, docId, null);
    }
    public byte[] downloadDocument(String packageId, String docId, String specificUserId) {
        return withEnvelopesApi((envelopesApi, account) -> {
            return doDownloadDocument(envelopesApi, account, packageId, docId);
        }, specificUserId);
    }

    private byte[] doDownloadDocument(EnvelopesApi envelopesApi, OAuth.Account account, String pkgId, String docId) {
        try {
            byte[] data = envelopesApi.getDocument(account.getAccountId(), pkgId, docId);
            return data;
        } catch (Exception ex) {
            String errMsg = "fail to download docusign package '" + pkgId + "' document '" + docId + "'";
            log.error(errMsg, ex);
            throw new AppException(errMsg, ex);
        }
    }

    <T> T withEnvelopesApi(BiFunction<EnvelopesApi, OAuth.Account, T> handler) {
        return withEnvelopesApi(handler, null);
    }

    <T> T withEnvelopesApi(BiFunction<EnvelopesApi, OAuth.Account, T> handler, String specificUserId) {
        return withDocuSignApi(handler, EnvelopesApi::new, specificUserId);
    }

    private <T> T withDocuSignApi(BiFunction<EnvelopesApi, OAuth.Account,T> handler,
                                  Supplier<EnvelopesApi> apiSupplier, String specificUserId) {
        ApiClient apiClient = new ApiClient(docuSignConfig.getBaseApiUrl());
        apiClient.setOAuthBasePath(docuSignConfig.getBaseOauthUrl());
        try {
           final String finalUser = StringUtils.firstNonBlank(specificUserId, docuSignConfig.getUserId());
           byte[] privateKey = pkiResolver.resolvePrivateKey();
           OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken(docuSignConfig.getClientId(), finalUser,
                   List.of(OAuth.Scope_SIGNATURE), privateKey, docuSignConfig.getOauthTokenExpiryInSeconds());
           apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());

           OAuth.UserInfo userInfo = apiClient.getUserInfo(oAuthToken.getAccessToken());
           OAuth.Account account = userInfo.getAccounts().get(0);
           apiClient.setBasePath(account.getBaseUri() + "/restapi");
           log.debug("Docusign account {} - {} will hit api base path: {}",
                   account.getAccountId(), account.getAccountName(), apiClient.getBasePath());
            Configuration.setDefaultApiClient(apiClient);
            return handler.apply(apiSupplier.get(), account);
        } catch (Exception ex) {
            if (ex instanceof AppException appEx) {
                throw appEx;
            } else {
                final String errMsg = "Fail to perform docusign api";
                log.error(errMsg, ex);
                throw new AppException(errMsg, ex);
            }
        }
    }
}
