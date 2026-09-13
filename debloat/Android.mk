LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := Debloat
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_OVERRIDES_PACKAGES += AmbientSensePrebuilt AppDirectedSMSService arcore DevicePolicyPrebuilt-v10334460 SwitchAccessPrebuilt_1.16.0.726766860
LOCAL_OVERRIDES_PACKAGES += CarrierSetup ConnMO ConnMetrics ScribePrebuilt_v8.4.773573318 TurboPrebuilt DeviceIntelligenceNetworkPrebuilt-astrea_20240329.00_RC02 DevicePersonalizationPrebuiltPixel2024-playstore_aiai_20250306.00_RC10
LOCAL_OVERRIDES_PACKAGES += DCMO  DiagnosticsToolPrebuilt DMService DeviceIntelligenceNetworkPrebuilt DevicePersonalizationPrebuilt 
LOCAL_OVERRIDES_PACKAGES += GCS GoogleCamera Jelly
LOCAL_OVERRIDES_PACKAGES += MaestroPrebuilt Maps MyVerizonServices NovaBugReportWrapper Ornament OemDmTrigger OBDM_Perm obdm_stub
LOCAL_OVERRIDES_PACKAGES += Phonograph
LOCAL_OVERRIDES_PACKAGES += SafetyHubPrebuilt SCONE ScribePrebuilt Showcase SimpleCalculator SimpleCalendar Snap SoundAmplifierPrebuilt SprintDM SprintHM
LOCAL_OVERRIDES_PACKAGES += Tag Tycho
LOCAL_OVERRIDES_PACKAGES += USCCDM
LOCAL_OVERRIDES_PACKAGES += Videos VZWAPNLib VzwOmaTrigger
LOCAL_OVERRIDES_PACKAGES += YouTubeMusicPrebuilt
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_SRC_FILES := /dev/null
include $(BUILD_PREBUILT)
