package java.nio.channels

import java.io.FileDescriptor

private[channels] trait FileDescriptorHandler {

  var fd: FileDescriptor

}
