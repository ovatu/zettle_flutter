#import "ZettlePlugin.h"
#if __has_include(<zettle/zettle-Swift.h>)
#import <zettle/zettle-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "zettle-Swift.h"
#endif

@implementation ZettlePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftZettlePlugin registerWithRegistrar:registrar];
}
@end
