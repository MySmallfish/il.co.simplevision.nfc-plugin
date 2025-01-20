platform :ios, '13.0'
use_frameworks!

target 'NFCDemo' do
  pod 'CoreNFC'
  pod 'Cordova'
end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['SWIFT_VERSION'] = '5.0'
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '13.0'
      config.build_settings['FRAMEWORK_SEARCH_PATHS'] ||= ['$(inherited)']
      config.build_settings['LIBRARY_SEARCH_PATHS'] ||= ['$(inherited)']
      config.build_settings['SWIFT_OBJC_BRIDGING_HEADER'] = '$(PODS_TARGET_SRCROOT)/src/ios/NfcPlugin-Bridging-Header.h'
    end
  end
end
