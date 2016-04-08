angular.module('puma')
    .filter('dto2page', function () {
        return function (client) {
            if (client.linearAlarm) {
                client.alarmStrategy = 'linear';
            } else if (client.exponentialAlarm) {
                client.alarmStrategy = 'exponential';
            } else {
                client.alarmStrategy = 'no';
            }

            return client;
        }
    })
    .filter('page2dto', function () {
        return function (client) {
            switch (client.alarmStrategy) {
                case 'linear':
                    client.linearAlarm = true;
                    break;
                case 'exponential':
                    client.exponentialAlarm = true;
                    break;
                case 'no':
                default:
                    client.noAlarm = true;
            }

            return client;
        }
    });
