package org.openforis.sepal.component.dataprovider

import groovy.transform.ToString


@ToString
class DownloadRequest implements Cloneable, Serializable {
    int requestId
    String requestName
    String username
    Date requestTime
    Boolean groupScenes
    String processingChain
    DataSet dataSet
    List<SceneRequest> scenes = []
    Status status

}