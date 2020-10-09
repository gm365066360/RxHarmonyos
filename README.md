# RxHarmonyos
根据鸿蒙线程间通信原理和RxAndroid相关类,改成RxHarmonyos,方便切换到UI线程(主线程)的操作

## 鸿蒙开发者文档中有介绍HarmonyOS的线程间通信 - EventHandler的运作机制
https://developer.harmonyos.com/cn/docs/documentation/doc-guides/inter-thread-overview-0000000000038958

EventHandler对应Handler
InnerEvent对应Message
EventRunner对应Looper
