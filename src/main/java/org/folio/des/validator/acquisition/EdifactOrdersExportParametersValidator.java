package org.folio.des.validator.acquisition;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.des.domain.dto.EdiConfig;
import org.folio.des.domain.dto.EdiSchedule;
import org.folio.des.domain.dto.ExportType;
import org.folio.des.domain.dto.ExportTypeSpecificParameters;
import org.folio.des.domain.dto.VendorEdiOrdersExportConfig;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static org.folio.des.domain.dto.VendorEdiOrdersExportConfig.FileFormatEnum.EDI;
import static org.folio.des.domain.dto.VendorEdiOrdersExportConfig.TransmissionMethodEnum.FTP;

@AllArgsConstructor
@Log4j2
@Service
public class EdifactOrdersExportParametersValidator implements Validator {
  private EdifactOrdersScheduledParamsValidator edifactOrdersScheduledParamsValidator;

  @Override
  public boolean supports(Class<?> aClass) {
    return ExportTypeSpecificParameters.class.isAssignableFrom(aClass);
  }

  @Override
  public void validate(Object target, Errors errors) {
    if (target == null) {
      String msg = String.format("%s type should contain %s parameters", ExportType.EDIFACT_ORDERS_EXPORT.getValue(),
        ExportTypeSpecificParameters.class.getSimpleName());
        errors.rejectValue(ExportTypeSpecificParameters.class.getSimpleName(), msg);
        throw new IllegalArgumentException(msg);
    }
    ExportTypeSpecificParameters specificParameters = (ExportTypeSpecificParameters) target;
    VendorEdiOrdersExportConfig vendorEdiOrdersExportConfig = specificParameters.getVendorEdiOrdersExportConfig();
    if (vendorEdiOrdersExportConfig == null) {
      String msg = String.format("%s type should contain %s parameters", ExportType.EDIFACT_ORDERS_EXPORT.getValue(),
                            VendorEdiOrdersExportConfig.class.getSimpleName());
      throw new IllegalArgumentException(msg);
    }

    validateFileFormat(vendorEdiOrdersExportConfig);

    EdiSchedule ediSchedule = vendorEdiOrdersExportConfig.getEdiSchedule();
    if (vendorEdiOrdersExportConfig.getEdiSchedule() != null &&
                  ediSchedule.getScheduleParameters() != null) {
      edifactOrdersScheduledParamsValidator.validate(ediSchedule.getScheduleParameters(), errors);
    }
  }

  private void validateFileFormat(VendorEdiOrdersExportConfig exportConfig) {
    var fileFormat = exportConfig.getFileFormat();
    if (fileFormat == null) {
      throw new IllegalArgumentException("Export configuration is incomplete, missing a file format");
    }

    if (fileFormat == EDI) {
      validateEdiConfig(exportConfig);
    }

    validateTransmissionType(exportConfig);
  }

  private void validateEdiConfig(VendorEdiOrdersExportConfig exportConfig) {
    var ediConfig = exportConfig.getEdiConfig();
    if (ediConfig != null) {
      if (CollectionUtils.isEmpty(ediConfig.getAccountNoList())) {
        throw new IllegalArgumentException("Export configuration is incomplete, missing Vendor Account Number(s)");
      }
      if (StringUtils.isEmpty(ediConfig.getLibEdiCode()) || StringUtils.isEmpty(ediConfig.getVendorEdiCode())) {
        throw new IllegalArgumentException("Export configuration is incomplete, missing library EDI code/Vendor EDI code");
      }
    }
  }

  private void validateTransmissionType(VendorEdiOrdersExportConfig exportConfig) {
    var transmissionMethod = exportConfig.getTransmissionMethod();
    if (transmissionMethod == null) {
      throw new IllegalArgumentException("Export configuration is incomplete, missing a transmission type");
    }

    if (transmissionMethod == FTP) {
      var ediFtp = exportConfig.getEdiFtp();
      if (ediFtp == null) {
        throw new IllegalArgumentException("Export configuration is incomplete, missing EDI FTP Properties");
      }

      if (ediFtp.getServerAddress() == null) {
        throw new IllegalArgumentException("Export configuration is incomplete, missing FTP/SFTP Server Address");
      }

      if (ediFtp.getFtpPort() == null) {
        throw new IllegalArgumentException("Export configuration is incomplete, missing FTP/SFTP Port");
      }
    }
  }

}
