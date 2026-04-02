# Amazon Q Prompt: CryptoPulse Test User Cleanup

## Context
I am cleaning up test user accounts from my CryptoPulse Android app backend. These are test registrations that need to be completely removed so the email IDs can be used for fresh registration.

## Backend Details
- **AWS Region**: ap-south-1
- **Cognito User Pool ID**: ap-south-1_21lFotrL0
- **DynamoDB Table**: cryptopulse-prod-users
- **App Name**: CryptoPulse

## Test Email IDs to Remove (10 total)

1. android-app-test@cryptopulse.dev
2. shrikananu@gmail.com
3. telangrocks@gmail.com
4. banyantree46@gmail.com
5. 46banyantree@gmail.com
6. test@cryptopulse.app
7. sandbox@cryptopulse.app
8. validation@cryptopulse.app
9. demo@cryptopulse.app
10. test-user@example.com

## What I Need Help With

I need to completely remove these users from BOTH:
1. **AWS Cognito User Pool** (authentication records)
2. **AWS DynamoDB** (user data records)

## Request

Please guide me step-by-step through the AWS Console to:

1. Navigate to Cognito User Pool and delete these 10 users
2. Navigate to DynamoDB and delete any records with these emails
3. Confirm the cleanup is complete
4. Verify these emails can now be used for fresh registration

Please provide exact navigation paths, button names, and any search terms I should use in the AWS Console.

## Verification Required

After cleanup, I should be able to:
- Use any of these email IDs for new registration
- Receive OTP emails without issues
- Have a completely fresh user record

Thank you!
