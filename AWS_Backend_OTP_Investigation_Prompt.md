# 🔍 AWS BACKEND OTP EMAIL ISSUE - INVESTIGATION PROMPT

## **ISSUE DESCRIPTION**
The CryptoPulse Android app is successfully registering users and navigating to the OTP verification screen, but users are **NOT receiving OTP emails**. The frontend (Android app) is working correctly - the issue is with the backend OTP email sending functionality.

## **CURRENT STATUS**
- ✅ **Android App**: Successfully built and deployed
- ✅ **User Registration**: API calls are working (email + dummy phone)
- ✅ **Navigation**: App correctly navigates to OTP screen
- ❌ **OTP Email**: Users are not receiving OTP verification emails

## **BACKEND DETAILS**
- **API Base URL**: `https://hs28uxr9j6.execute-api.ap-south-1.amazonaws.com/prod`
- **Registration Endpoint**: `POST /auth` with action="register"
- **Cognito User Pool**: `ap-south-1_21lFotrL0`
- **Test Email**: `android-app-test@cryptopulse.dev`
- **Region**: `ap-south-1`

## **KNOWN PREVIOUS ISSUES**
1. **DynamoDB Reserved Keyword**: There was a previous issue with the `verified` field being a reserved keyword in DynamoDB
2. **Lambda Functions**: Multiple Cognito trigger functions may need updates:
   - `cryptopulse-prod-cognito-trigger`
   - `cryptopulse-prod-auto-confirm-trigger` 
   - `cryptopulse-prod-auth-handler`

## **INVESTIGATION TASKS**

### **1. Check Cognito User Pool Configuration**
```bash
# Verify Cognito User Pool settings
aws cognito-idp describe-user-pool --user-pool-id ap-south-1_21lFotrL0 --region ap-south-1
```

### **2. Check Lambda Trigger Configuration**
```bash
# Check if Lambda triggers are properly configured
aws cognito-idp describe-user-pool-client --user-pool-id ap-south-1_21lFotrL0 --client-id 2fl6ql10rre31flqi1504evt1b --region ap-south-1
```

### **3. Test Lambda Function Logs**
```bash
# Check CloudWatch logs for OTP sending issues
aws logs describe-log-groups --log-group-name-prefix /aws/lambda/cryptopulse-prod --region ap-south-1

# Get recent logs for the auth handler
aws logs filter-log-events --log-group-name /aws/lambda/cryptopulse-prod-auth-handler --region ap-south-1 --start-time $(date -d '1 hour ago' +%s)000
```

### **4. Verify Email Service Configuration**
```bash
# Check SES configuration and sending limits
aws ses get-send-quotas --region ap-south-1

# Verify email identity is verified
aws ses get-identity-verification-attributes --identities android-app-test@cryptopulse.dev --region ap-south-1
```

### **5. Test Registration Flow**
```bash
# Test the registration endpoint directly
curl -X POST https://hs28uxr9j6.execute-api.ap-south-1.amazonaws.com/prod/auth \
  -H "Content-Type: application/json" \
  -d '{
    "action": "register",
    "intent": "REGISTER", 
    "email": "android-app-test@cryptopulse.dev",
    "password": "AndroidTest123!",
    "phone_number": "+919876543210"
  }'
```

## **EXPECTED RESOLUTION**
1. **Fix Lambda Functions**: Update any Lambda functions that handle OTP generation and email sending
2. **Resolve DynamoDB Issues**: Ensure the `verified` field issue is properly resolved with `ExpressionAttributeNames`
3. **Verify SES Configuration**: Ensure Amazon SES is properly configured to send emails
4. **Test End-to-End**: Confirm that users receive OTP emails after registration

## **SUCCESS CRITERIA**
- ✅ User registers with email
- ✅ OTP email is sent to user's inbox
- ✅ User can verify OTP and complete registration
- ✅ User can login with email and password

## **PRIORITY LEVEL**
**HIGH** - This is blocking user registration and onboarding

---

**Please provide the specific AWS CLI commands needed to investigate and resolve this OTP email sending issue, focusing on the Cognito, Lambda, and SES configurations.**
