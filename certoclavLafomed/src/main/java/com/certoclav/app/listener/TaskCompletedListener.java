package com.certoclav.app.listener;

import com.certoclav.app.database.Protocol;




public interface TaskCompletedListener {
 void onTaskCompleted(Boolean result, Protocol protocol);
}
