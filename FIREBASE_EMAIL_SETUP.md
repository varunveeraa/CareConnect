# Firebase Email Template Customization

## Setting up Custom Password Reset Email Template

### Step 1: Access Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project (CareConnect)
3. Navigate to **Authentication** → **Templates**

### Step 2: Customize Password Reset Template

1. Click on **Password reset** template
2. Customize the following elements:

#### Email Subject

```
Reset your CareConnect password
```

#### Email Body Template

```html
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
  <div style="text-align: center; margin-bottom: 30px;">
    <h1 style="color: #2196F3; margin: 0;">CareConnect</h1>
    <p style="color: #666; margin: 5px 0;">Your Health, Our Priority</p>
  </div>
  
  <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
    <h2 style="color: #333; margin-top: 0;">Password Reset Request</h2>
    <p style="color: #555; line-height: 1.6;">
      Hello,<br><br>
      We received a request to reset your CareConnect account password. If you made this request, 
      click the button below to reset your password:
    </p>
  </div>
  
  <div style="text-align: center; margin: 30px 0;">
    <a href="%LINK%" style="background-color: #2196F3; color: white; padding: 12px 30px; 
       text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
      Reset Password
    </a>
  </div>
  
  <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; 
              border-radius: 5px; margin: 20px 0;">
    <p style="color: #856404; margin: 0; font-size: 14px;">
      <strong>Security Notice:</strong> This link will expire in 1 hour for your security. 
      If you didn't request this reset, please ignore this email.
    </p>
  </div>
  
  <p style="color: #666; font-size: 14px; line-height: 1.6;">
    If the button doesn't work, copy and paste this link into your browser:<br>
    <a href="%LINK%" style="color: #2196F3; word-break: break-all;">%LINK%</a>
  </p>
  
  <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
  
  <div style="text-align: center; color: #999; font-size: 12px;">
    <p>CareConnect - Connecting Care, Empowering Health</p>
    <p>If you have any questions, contact our support team.</p>
  </div>
</div>
```

### Step 3: Email Verification Template (Optional)

You can also customize the email verification template:

#### Subject

```
Verify your CareConnect email address
```

#### Body Template

```html
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
  <div style="text-align: center; margin-bottom: 30px;">
    <h1 style="color: #2196F3; margin: 0;">CareConnect</h1>
    <p style="color: #666; margin: 5px 0;">Welcome to Your Health Journey</p>
  </div>
  
  <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
    <h2 style="color: #333; margin-top: 0;">Welcome to CareConnect!</h2>
    <p style="color: #555; line-height: 1.6;">
      Thank you for joining CareConnect. To complete your registration and start managing 
      your health data, please verify your email address by clicking the button below:
    </p>
  </div>
  
  <div style="text-align: center; margin: 30px 0;">
    <a href="%LINK%" style="background-color: #4CAF50; color: white; padding: 12px 30px; 
       text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
      Verify Email Address
    </a>
  </div>
  
  <div style="background-color: #e8f5e8; border: 1px solid #c3e6c3; padding: 15px; 
              border-radius: 5px; margin: 20px 0;">
    <p style="color: #2e7d2e; margin: 0; font-size: 14px;">
      <strong>What's Next?</strong> After verification, you can start tracking your health, 
      connecting with healthcare providers, and accessing personalized health insights.
    </p>
  </div>
  
  <p style="color: #666; font-size: 14px; line-height: 1.6;">
    If the button doesn't work, copy and paste this link into your browser:<br>
    <a href="%LINK%" style="color: #2196F3; word-break: break-all;">%LINK%</a>
  </p>
  
  <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
  
  <div style="text-align: center; color: #999; font-size: 12px;">
    <p>CareConnect - Connecting Care, Empowering Health</p>
    <p>If you didn't create this account, you can safely ignore this email.</p>
  </div>
</div>
```

### Step 4: Preview and Test

1. Use the **Send test email** feature in Firebase Console
2. Test with a real email address
3. Check spam/junk folders
4. Verify links work correctly

### Step 5: Additional Customization Options

- **Sender Name**: Set to "CareConnect Team"
- **Reply-to Email**: Set your support email
- **Custom Domain**: Use your own domain for professional appearance

### Notes

- Firebase uses `%LINK%` as a placeholder for the actual reset/verification link
- Templates support basic HTML and inline CSS
- Changes take effect immediately
- Test thoroughly before going live

### Alternative: Custom Email Service

If you need more advanced templating, consider:

- SendGrid with custom templates
- AWS SES with Lambda functions
- Custom backend with email service integration