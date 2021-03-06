<?php

Class DashboardController extends \BaseController {

    //uses layout views/layouts/sdmsdashboard.blade.phhp
    protected $layout = 'layouts.master';

    //shows the dashboard
    public function showDashboard() {

        $userName = Session::get('username');

        $fullFolderPath = "/data/home/" . $userName;
        exec("sudo ls " . $fullFolderPath, $userFolder);
        exec("sudo du -aL " . $fullFolderPath, $sizeArray);

        $subFolderArray = explode("/", $fullFolderPath);
        $currentFolderDepth = count($subFolderArray);

        $resultSize = array();
        $currentFolderDepth = $currentFolderDepth + 1;
        foreach ($sizeArray as $keyArr => $valArr) {
            $subFoldValArray = explode("/", $valArr);
            $virtualFolderDepth = count($subFoldValArray);
            if ($currentFolderDepth == $virtualFolderDepth) {
                $tempKey = $virtualFolderDepth - 1;
                $resultSize[$subFoldValArray[$tempKey]] = $subFoldValArray[0];
            }
        }

        $data['userFolder'] = $userFolder;
        $data['folderPath'] = $fullFolderPath;
        $data['userName'] = $userName;
        $data['userFolderSize'] = $resultSize;
        $this->layout->content = View::make('dashboard.dashboard', $data);
    }

    public function clearTempDownloadRepo() {
        $this->layout = '';
        $expiryDate = mktime(date("H") - 5, date("i"), date("s"), date("m"), date("d"), date("Y"));
        $expiryDate = date("Y-m-d H:i:s", $expiryDate);

        $logDatas = DownloadLog::where('created_at', '<=', $expiryDate)->get();
        if (count($logDatas) > 0) {
            foreach ($logDatas as $rowLogData) {
                $userName = $rowLogData->username;
                $filePath = $rowLogData->filepath;
                if (file_exists($filePath)) {
                    exec("sudo rm -R $filePath");
                    //delete  from database repo too
                    DownloadLog::where('username', '=', $userName)
                        ->where('filepath', '=', $filePath)
                        ->delete();
                }
            }
        }
    }

    public function migrationStatus($sceneId = NULL) {
        return View::make('dashboard.downloadStatus');
    }

    public function migrationChecker() {
        $this->layout = '';
        $sceneRequest = Session::get('sceneRequest');
        $processingScript = $sceneRequest['processingScript'];
        $requestSceneTemp = $sceneRequest['scenes'];
        if (is_array($requestSceneTemp) && count($requestSceneTemp) > 0) {
            #todo remove session
            $requestScene = array();
            foreach ($requestSceneTemp as $key => $value) {
                $sceneTrack = explode("-", $value);
                $sceneIdId = $sceneTrack[0];
                $datasetId = $sceneTrack[1];
                $requestScene[] = $datasetId . '-' . $sceneIdId;
            }
            $sceneSessionId = implode(",", $requestScene);
            Session::forget('sceneRequest');
            $data = "{\"scenes\": \"$sceneSessionId\", \"processingScript\": \"$processingScript\"}";
            echo $data;
        } else {
            echo 'no';
        }
    }


    public function migratecheck() {
        $this->layout = '';
        $requestScene = Input::get('requestScene');
        $requestScene = explode(",", $requestScene);

        $sceneArra = array();
        foreach ($requestScene as $keyScenes => $valueScenes) {
            if (strlen($valueScenes) > 5) {

                $sceneArra[] = $valueScenes;
            }
        }
        $processingScript = Input::get('processingScript');
        $sceneRequest = array('scenes' => $sceneArra, 'processingScript' => $processingScript);
        Session::put('sceneRequest', $sceneRequest);
    }

    public function downloadFileFromDashboard($filePath = NULL) {
        $userName = Session::get('username');
        $filePath = base64_decode($filePath);
        $file = $filePath;
        Logger::debug("Downloading file. User: '$userName', Path: '$filePath', File: '$file'");
        if (!starts_with($file, "/data/home/$userName/")) {
            Logger::warn("Trying to download file outside of homeDir. Username: '$userName', File: '$file'");
            echo 'Sorry, file not available for download now. Please go back to <a href="https://fao-dm.org/dashboard">Dashboard</a>';
            exit;
        }

        if (file_exists($file)) {
            Logger::debug("File exists $file");
            header('Content-Description: File Transfer');
            header('Content-Type: application/octet-stream');
            header('Content-Disposition: attachment; filename=' . basename($file));
            header('Content-Transfer-Encoding: binary');
            header('Expires: 0');
            header('Cache-Control: must-revalidate');
            header('Pragma: public');
            header('Content-Length: ' . filesize($file));
            readfile($file);
            exit;
        } else {
            Logger::warn("File not found $file");
            echo 'Sorry, file not available for download now. Please go back to <a href="https://fao-dm.org/dashboard">Dashboard</a>';
            exit;
        }
    }

    //shows the dashboard
    public function createZipFromDashboard() {
        $filePath = Input::get('currentLink');
        $this->layout = '';
        $userName = Session::get('username');

        $filePath = base64_decode($filePath);
        $filePathSrc = "/data/home/$userName/$filePath/";

        $tempFolderPath = "../../../../data/sdms-data-repo/temp-data-repo/";

        $tempFolderPathUser = "../../../../data/sdms-data-repo/temp-data-repo/$userName/";

        if (!file_exists($tempFolderPathUser)) {
            exec("sudo mkdir $tempFolderPathUser");
        }

        $fileNameArray = explode("/", $filePathSrc);
        $filenameDiretoryKey = count($fileNameArray);
        $folderDirectoryName = $fileNameArray[$filenameDiretoryKey - 2];
        $compresName = $folderDirectoryName . ".tar.gz";
        $compFilePath = $tempFolderPathUser . $compresName;

        if (!file_exists($compFilePath)) {
            exec("sudo rm $compFilePath");
            //delete  from database repo too
            DownloadLog::where('username', '=', $userName)
                ->where('filepath', '=', $compFilePath)
                ->delete();
        }

        exec("sudo tar zcvf $compFilePath $filePathSrc", $output, $zipStatus);

        if (!$zipStatus) {
            echo 'sucess';
            //insert into downloadlog 		
            $downloadLogModel = new DownloadLog;
            $downloadLogModel->username = $userName;
            $downloadLogModel->filepath = $compFilePath;
            $downloadLogModel->save();
        } else {
            echo 'error';
        }
    }

    public function downloadZipFromDashboard($filePath = NULL) {

        $this->layout = '';
        $userName = Session::get('username');

        $filePath = base64_decode($filePath);
        $filePathSrc = "/data/home/$userName/$filePath/";

        $tempFolderPathUser = "../../../../data/sdms-data-repo/temp-data-repo/$userName/";
        $fileNameArray = explode("/", $filePathSrc);
        $filenameDiretoryKey = count($fileNameArray);
        $folderDirectoryName = $fileNameArray[$filenameDiretoryKey - 2];
        $compresName = $folderDirectoryName . ".tar.gz";
        $compFilePath = $tempFolderPathUser . $compresName;

        if (file_exists($compFilePath)) {
            header('Content-Description: File Transfer');
            header('Content-Type: application/octet-stream');
            header('Content-Disposition: attachment; filename=' . basename($compFilePath));
            header('Content-Transfer-Encoding: binary');
            header('Expires: 0');
            header('Cache-Control: must-revalidate');
            header('Pragma: public');
            header('Content-Length: ' . filesize($compFilePath));
            readfile($compFilePath);
            exit;
        } else {
            echo 'Sorry, file not available for download now. Please go back to <a href="https://fao-dm.org/dashboard">Dashboard</a>';
            exit;
        }
    }

    public function showFolders() {
        $this->layout = '';
        $folderPath = Input::get('folderPath');
        Logger::debug('showFolders: ' . $folderPath);
        $userName = Session::get('username');
        $homePath = "/data/home/" . $userName;
        if (0 !== strpos($folderPath, $homePath))
            $folderPath = $homePath;
        exec("sudo ls " . $folderPath, $userFolder);
        exec("sudo du -aL " . $folderPath, $sizeArray);

        $subFolderArray = explode("/", $folderPath);
        $currentFolderDepth = count($subFolderArray);

        $resultSize = array();
        $currentFolderDepth = $currentFolderDepth + 1;
        foreach ($sizeArray as $keyArr => $valArr) {
            $subFoldValArray = explode("/", $valArr);
            $virtualFolderDepth = count($subFoldValArray);

            if ($currentFolderDepth == $virtualFolderDepth) {
                $tempKey = $virtualFolderDepth - 1;
                $resultSize[$subFoldValArray[$tempKey]] = $subFoldValArray[0];
            }
        }

        $data['userFolder'] = $userFolder;
        $data['folderPath'] = $folderPath;
        $data['userName'] = $userName;
        $data['userFolderSize'] = $resultSize;
        return View::make('dashboard.showfolders', $data);
    }

    function recursive_array_search($needle, $haystack) {
        foreach ($haystack as $key => $value) {
            $current_key = $key;
            if ($needle === $value OR (is_array($value) && recursive_array_search($needle, $value) !== false)) {
                return $current_key;
            }
        }
        return false;
    }

    public function showSubFolders() {


        $this->layout = '';
        $folderPath = Input::get('folderPath');
        $userName = Session::get('username');
        exec("sudo ls " . $folderPath, $userFolder);


        $data['userName'] = $userName;
        $data['userFolder'] = $userFolder;
        $data['folderPath'] = $folderPath;
        return View::make('dashboard.subfolders', $data);
    }

    public function changePassword() {
        $userName = Session::get('username');
        $this->layout->content = View::make('password.passwordForm');
    }

    public function checkPassword() {

        // Validation rules
        $rules = array(
            'oldPassword' => 'required',
            'newPassword' => 'required',
            'repPassword' => 'required',
        );

        // Validate the inputs
        $v = Validator::make(Input::all(), $rules);

        // Setting attribute for readable format   
        $attributeNames = array(
            'oldPassword' => 'old password',
            'newPassword' => 'new password',
            'repPassword' => 'confirm password',
        );
        $v->setAttributeNames($attributeNames);

        // Was the validation successful?
        if ($v->fails()) {
            // Something went wrong
            return Redirect::to('password')
                ->withErrors($v)
                ->withInput();
        } else {
            $user = array(
                'username' => Session::get('username'),
                'oldPassword' => Input::get('oldPassword'),
                'newPassword' => Input::get('newPassword'),
                'repPassword' => Input::get('repPassword')
            );
            //Check user credentials with pam - system data- linux user
            if (!pam_auth($user['username'], $user['oldPassword'])) {
                $message = 'Incorrect password!';
                $this->layout->content = View::make('password.passwordForm', array('message' => $message));
            } else {

                $userAuth = User::where('username', $user['username'])->first();
                if (count($userAuth) > 0) {
                    $userAuth = User::where('username', $user['username'])->first();
                } else {

                    //Create new user based in the username	
                    $userModel = new User;
                    $userModel->username = $user['username'];
                    $userModel->save();
                    //Get details from database to trigger laravel auth class	
                    $userAuth = User::where('username', $user['username'])->first();
                }

                //bypassing authentication with username alone instead of password to relate user
                Auth::login($userAuth); //Authentication progress
                Session::put('username', $user['username']);
                return Redirect::to('account');
                // The user is active, not suspended, and exists.
            }
        }
    }

}
