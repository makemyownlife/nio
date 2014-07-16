/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diyicai.nioapi.net;

/**
 * 错误码
 */
public interface ErrorCode {

    // cobar error code
    int ERR_OPEN_SOCKET = 3001;
    int ERR_CONNECT_SOCKET = 3002;
    int ERR_FINISH_CONNECT = 3003;
    int ERR_REGISTER = 3004;
    int ERR_READ = 3005;
    int ERR_PUT_WRITE_QUEUE = 3006;
    int ERR_WRITE_BY_EVENT = 3007;
    int ERR_WRITE_BY_QUEUE = 3008;
    int ERR_HANDLE_DATA = 3009;

}
