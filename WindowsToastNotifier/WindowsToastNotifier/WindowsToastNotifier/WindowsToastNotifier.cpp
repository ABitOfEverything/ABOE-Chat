#include "stdafx.h"
#include "wintoast\wintoastlib.h"
#include "jni\me_glorantq_aboe_chat_client_notification_WindowsToastNotifier.h"
#include <AtlBase.h>
#include <atlconv.h>
#include <iostream>

std::wstring strToWStr(std::string in) {
	CA2W ca2w(in.c_str(), CP_UTF8);
	std::wstring w = ca2w.m_szBuffer;

	return w;
}

class WinToastListnener : public WinToastLib::IWinToastHandler {
	void toastActivated() const override {

	}

	void toastActivated(int actionIndex) const override {

	}

	void toastDismissed(WinToastDismissalReason state) const override {

	}

	void toastFailed() const override {

	}
};

JNIEXPORT jboolean JNICALL Java_me_glorantq_aboe_chat_client_notification_WindowsToastNotifier_initialise(JNIEnv* env, jobject object) {
	if (!WinToastLib::WinToast::instance()->isCompatible()) {
		std::cerr << "Not compatible" << std::endl;
		return false;
	}

	std::wstring aumid = L"me.glorantq.aboe.chat.notification.win10";
	WinToastLib::WinToast::instance()->setAppName(L"A Bit of Everything");
	WinToastLib::WinToast::instance()->setAppUserModelId(aumid);

	return WinToastLib::WinToast::instance()->initialize();
}

JNIEXPORT void JNICALL Java_me_glorantq_aboe_chat_client_notification_WindowsToastNotifier_showNotificationNative(JNIEnv* env, jobject object, jstring logoPath, jstring header, jstring message) {
	const char* headerChars = env->GetStringUTFChars(header, false);
	const char* messageChars = env->GetStringUTFChars(message, false);
	const char* logoPathChars = env->GetStringUTFChars(logoPath, false);

	std::wstring headerW = strToWStr(headerChars);
	std::wstring messageW = strToWStr(messageChars);
	std::wstring logoPathW = strToWStr(logoPathChars);

	WinToastLib::WinToastTemplate toastTemplate = WinToastLib::WinToastTemplate(WinToastLib::WinToastTemplate::ImageAndText02);
	toastTemplate.setTextField(headerW, WinToastLib::WinToastTemplate::FirstLine);
	toastTemplate.setTextField(messageW, WinToastLib::WinToastTemplate::SecondLine);
	toastTemplate.setImagePath(logoPathW);

	WinToastLib::WinToast::instance()->showToast(toastTemplate, new WinToastListnener);
}