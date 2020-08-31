package com.cd.room.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**应该执行某种动作如碰、杠的动作的人对应的下标及动作类型
 * */
public class ActionTask {
    private AtomicReference<String> performer = new AtomicReference<>();//该执行动作的人的名字
    private AtomicReferenceArray<TaskType> taskTypes = new AtomicReferenceArray<>(6);//任务类型. 最多同时只有三种任务 出牌、碰、杠
    public enum TaskType{
        PLAY,PENG,GANG,AN_GANG,BA_GANG,HU
    }

    public ActionTask setPerformer(String name) {
        performer.set(name);
        return this;
    }

    public String getPerformer(){
        return performer.get();
    }

    public ActionTask setTaskType(TaskType type){
        taskTypes.set(type.ordinal(), type);
        return this;
    }

    public void removeAllTask(){
        for (int i = 0; i < taskTypes.length(); i++){
            taskTypes.set(i, null);
        }
    }

    public List<TaskType> getTaskType() {
        List<TaskType> taskTypeList = new ArrayList<>();
        for(TaskType type : TaskType.values()){
            if (taskTypes.get(type.ordinal()) != null){
                taskTypeList.add(type);
            }
        }
        return taskTypeList;
    }

}
